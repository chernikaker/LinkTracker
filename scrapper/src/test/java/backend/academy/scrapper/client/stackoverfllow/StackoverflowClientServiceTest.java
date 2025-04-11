package backend.academy.scrapper.client.stackoverfllow;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class StackoverflowClientServiceTest {

    @Mock
    private ExternalClient client;

    @InjectMocks
    private StackoverflowClientService stackoverflowClientService;

    private Link link;

    @BeforeEach
    public void setUp() {
        link = new Link(
                "https://stackoverflow.com/questions/12345",
                LinkType.STACKOVERFLOW,
                LocalDateTime.now(ZoneId.systemDefault()));
    }

    @Test
    public void getAllInfo_DataIsValid() {
        String commentJson = "{\"items\": [{\"owner\":{\"display_name\":\"user1\"},\"creation_date\":1696156800, \"body\":\"body\"}]}";
        String answerJson = "{\"items\": [{\"owner\":{\"display_name\":\"user2\"},\"creation_date\":1696156800, \"body\":\"body\"}]}";
        String dataJson = "{\"items\": [{\"title\":\"title\"}]}";

        when(client.getResponse("/questions/12345")).thenReturn(dataJson);
        when(client.getResponse("/questions/12345/comments")).thenReturn(commentJson);
        when(client.getResponse("/questions/12345/answers")).thenReturn(answerJson);

        List<UpdateInfo> updates = stackoverflowClientService.getAllInfo(link);
        assertEquals(2, updates.size());

        UpdateInfo commentUpdate = updates.getFirst();
        assertEquals("title", commentUpdate.title());
        assertEquals("user1", commentUpdate.authorName());
        assertEquals(UpdateInfoType.COMMENT, commentUpdate.type());

        UpdateInfo answerUpdate = updates.get(1);
        assertEquals("title", commentUpdate.title());
        assertEquals("user2", answerUpdate.authorName());
        assertEquals(UpdateInfoType.ANSWER, answerUpdate.type());
    }

    @Test
    public void getAllInfo_invalidJson() {
        String invalidJson = "invalid json";
        when(client.getResponse("/questions/12345")).thenReturn(invalidJson);

        assertThatThrownBy(() -> stackoverflowClientService.getAllInfo(link))
                .isInstanceOf(ScrapperInternalResponseException.class);
    }

    @Test
    public void getAllInfo_httpClientError() {
        HttpClientErrorException httpClientErrorException =
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, null, null);
        when(client.getResponse("/questions/12345")).thenThrow(httpClientErrorException);

        assertThrows(ScrapperInternalResponseException.class, () -> stackoverflowClientService.getAllInfo(link));
    }

    @Test
    public void isLinkAvailable_validLink() {
        when(client.getResponse("/questions/12345")).thenReturn("{}");

        boolean isAvailable = stackoverflowClientService.isLinkAvailable(link);

        assertTrue(isAvailable);
    }

    @Test
    public void isLinkAvailable_accessError() {
        when(client.getResponse("/questions/12345")).thenThrow(HttpClientErrorException.class);

        boolean isAvailable = stackoverflowClientService.isLinkAvailable(link);

        assertFalse(isAvailable);
    }
}
