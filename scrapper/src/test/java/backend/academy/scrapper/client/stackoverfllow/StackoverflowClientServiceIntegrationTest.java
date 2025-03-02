package backend.academy.scrapper.client.stackoverfllow;

import backend.academy.scrapper.client.ClientTestConfig;
import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@SpringBootTest(
    classes = {StackoverflowClientService.class}
)
@Import(ClientTestConfig.class)
@WireMockTest
public class StackoverflowClientServiceIntegrationTest {

    @Autowired
    private StackoverflowClientService soClientService;

    @Autowired
    private WireMockServer wireMockServer;


    @Test
    public void testGetAllInfo_HttpClientErrorException() {
        wireMockServer.stubFor(get(urlEqualTo("/questions/123/comments?key=key&site=stackoverflow"))
            .willReturn(aResponse().withStatus(404)));
        Link link = new Link("https://stackoverflow.com/questions/123", LinkType.STACKOVERFLOW, LocalDateTime.now(ZoneId.systemDefault()));

        assertThatThrownBy(() -> soClientService.getAllInfo(link))
            .isInstanceOf(ScrapperInternalResponseException.class);
    }

    @Test
    public void testGetAllInfo_Success() {
        wireMockServer.stubFor(get(urlEqualTo("/questions/123/comments?key=key&site=stackoverflow"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"items\":[{\"owner\":{\"display_name\":\"testUser\"},\"creation_date\":1696118400}]}")));
        wireMockServer.stubFor(get(urlEqualTo("/questions/123/answers?key=key&site=stackoverflow"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"items\":[{\"owner\":{\"display_name\":\"testUser\"},\"creation_date\":1696118400}]}")));
        Link link = new Link("https://stackoverflow.com/questions/123", LinkType.STACKOVERFLOW, LocalDateTime.now(ZoneId.systemDefault()));

        List<UpdateInfo> result = assertDoesNotThrow(() -> soClientService.getAllInfo(link));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

}
