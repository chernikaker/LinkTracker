package backend.academy.scrapper.client.stackoverfllow;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.scrapper.client.WireMockClientTestConfig;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.model.UpdateInfo;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = {StackoverflowClientService.class})
@Import(WireMockClientTestConfig.class)
@WireMockTest
public class StackoverflowClientServiceIntegrationTest {

    @Autowired
    private StackoverflowClientService soClientService;

    @Autowired
    private WireMockServer wireMockServer;

    @Test
    public void testGetAllInfo_HttpClientErrorException() {
        wireMockServer.stubFor(get(urlEqualTo("/questions/123/comments?key=key&site=stackoverflow&filter=withbody"))
                .willReturn(aResponse().withStatus(404)));
        Link link = new Link(
                "https://stackoverflow.com/questions/123",
                LinkType.STACKOVERFLOW,
                LocalDateTime.now(ZoneId.systemDefault()));

        assertThatThrownBy(() -> soClientService.getAllInfo(link))
                .isInstanceOf(ScrapperInternalResponseException.class);
    }

    @Test
    public void testGetAllInfo_Success() {
        setWireMockSuccessAnswerWithJson(
            "/questions/123?key=key&site=stackoverflow&filter=withbody",
            "{\"items\": [{\"title\":\"title\"}]}"
        );
        setWireMockSuccessAnswerWithJson(
            "/questions/123/comments?key=key&site=stackoverflow&filter=withbody",
            "{\"items\": [{\"owner\":{\"display_name\":\"user1\"},\"creation_date\":1696156800, \"body\":\"body\"}]}"
        );

        setWireMockSuccessAnswerWithJson(
            "/questions/123/answers?key=key&site=stackoverflow&filter=withbody",
            "{\"items\": [{\"owner\":{\"display_name\":\"user2\"},\"creation_date\":1696156800, \"body\":\"body\"}]}"
        );

        Link link = new Link(
                "https://stackoverflow.com/questions/123",
                LinkType.STACKOVERFLOW,
                LocalDateTime.now(ZoneId.systemDefault()));

        List<UpdateInfo> result = assertDoesNotThrow(() -> soClientService.getAllInfo(link));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    private void setWireMockSuccessAnswerWithJson(String request, String response){
        wireMockServer.stubFor(
            get(urlEqualTo(request))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));
    }
}
