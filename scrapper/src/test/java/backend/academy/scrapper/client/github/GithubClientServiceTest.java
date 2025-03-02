package backend.academy.scrapper.client.github;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.dto.UpdateInfoType;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.GithubResponseJsonIsInvalid;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
public class GithubClientServiceTest {

    @Mock
    private GithubClient githubClient;

    @InjectMocks
    private GithubClientService githubClientService;

    private Link link;

    @BeforeEach
    public void setUp() {
        link = new Link("https://github.com/author/repo", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
    }

    @Test
    public void getAllInfo_DataIsValid() {
        String commitJson =
                "[{\"commit\":{\"message\":\"Commit message\",\"author\":{\"date\":\"2023-10-01T12:00:00Z\"}},\"author\":{\"login\":\"author1\"}}]";
        String issueJson =
                "[{\"title\":\"Issue title\",\"user\":{\"login\":\"author2\"},\"created_at\":\"2023-10-01T12:00:00Z\"}]";
        String commentJson =
                "[{\"body\":\"Comment body\",\"user\":{\"login\":\"author3\"},\"created_at\":\"2023-10-01T12:00:00Z\"}]";
        when(githubClient.getResponse("repos/author/repo/commits")).thenReturn(commitJson);
        when(githubClient.getResponse("repos/author/repo/issues")).thenReturn(issueJson);
        when(githubClient.getResponse("repos/author/repo/comments")).thenReturn(commentJson);

        List<UpdateInfo> updates = assertDoesNotThrow(() -> githubClientService.getAllInfo(link));

        assertEquals(3, updates.size());

        UpdateInfo commentUpdate = updates.getFirst();
        assertEquals("Comment body", commentUpdate.message());
        assertEquals("author3", commentUpdate.authorName());
        assertEquals(UpdateInfoType.COMMENT, commentUpdate.type());

        UpdateInfo issueUpdate = updates.get(1);
        assertEquals("Issue title", issueUpdate.message());
        assertEquals("author2", issueUpdate.authorName());
        assertEquals(UpdateInfoType.ISSUE, issueUpdate.type());

        UpdateInfo commitUpdate = updates.get(2);
        assertEquals("Commit message", commitUpdate.message());
        assertEquals("author1", commitUpdate.authorName());
        assertEquals(UpdateInfoType.COMMIT, commitUpdate.type());
    }

    @Test
    public void getAllInfo_invalidJson() {
        String invalidJson = "invalid json";
        when(githubClient.getResponse("repos/author/repo/commits")).thenReturn(invalidJson);

        assertThatThrownBy(() -> githubClientService.getAllInfo(link)).isInstanceOf(GithubResponseJsonIsInvalid.class);
    }

    @Test
    public void getAllInfo_httpClientError() {
        HttpClientErrorException httpClientErrorException =
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, null, null);
        when(githubClient.getResponse("repos/author/repo/commits")).thenThrow(httpClientErrorException);

        assertThatThrownBy(() -> githubClientService.getAllInfo(link))
                .isInstanceOf(ScrapperInternalResponseException.class);
    }

    @Test
    public void isLinkAvailable_validLink() {
        when(githubClient.getResponse("repos/author/repo")).thenReturn("{}");

        boolean isAvailable = githubClientService.isLinkAvailable(link);

        assertTrue(isAvailable);
    }

    @Test
    public void isLinkAvailable_accessError() {
        when(githubClient.getResponse("repos/author/repo")).thenThrow(HttpClientErrorException.class);

        boolean isAvailable = githubClientService.isLinkAvailable(link);

        assertFalse(isAvailable);
    }
}
