package backend.academy.scrapper.client.github;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
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
    private ExternalClient githubClient;

    @InjectMocks
    private GithubClientService githubClientService;

    private Link link;

    @BeforeEach
    public void setUp() {
        link = new Link("https://github.com/author/repo", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
    }

    @Test
    public void getAllInfo_DataIsValid() {
        String prJson =
                "[{\"title\":\"Pull request title\",\"user\":{\"login\":\"author1\"},\"created_at\":\"2023-10-01T12:00:00Z\", \"body\":\"body\"}]";
        String issueJson =
                "[{\"title\":\"Issue title\",\"user\":{\"login\":\"author2\"},\"created_at\":\"2023-10-01T12:00:00Z\", \"body\":\"body\"}]";
        when(githubClient.getResponse("repos/author/repo/pulls")).thenReturn(prJson);
        when(githubClient.getResponse("repos/author/repo/issues")).thenReturn(issueJson);

        List<UpdateInfo> updates = assertDoesNotThrow(() -> githubClientService.getAllInfo(link));

        assertEquals(2, updates.size());

        UpdateInfo prUpdate = updates.getFirst();
        assertEquals("Pull request title", prUpdate.title());
        assertEquals("body", prUpdate.message());
        assertEquals("author1", prUpdate.authorName());
        assertEquals(UpdateInfoType.PULL_REQUEST, prUpdate.type());

        UpdateInfo issueUpdate = updates.get(1);
        assertEquals("Issue title", issueUpdate.title());
        assertEquals("body", issueUpdate.message());
        assertEquals("author2", issueUpdate.authorName());
        assertEquals(UpdateInfoType.ISSUE, issueUpdate.type());
    }

    @Test
    public void getAllInfo_invalidJson() {
        String invalidJson = "invalid json";
        when(githubClient.getResponse("repos/author/repo/issues")).thenReturn(invalidJson);

        assertThatThrownBy(() -> githubClientService.getAllInfo(link))
                .isInstanceOf(ScrapperInternalResponseException.class);
    }

    @Test
    public void getAllInfo_httpClientError() {
        HttpClientErrorException httpClientErrorException =
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, null, null);
        when(githubClient.getResponse("repos/author/repo/issues")).thenThrow(httpClientErrorException);

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
