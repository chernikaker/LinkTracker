package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.ClientTestConfig;
import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
    classes = {GithubClientService.class}
)
@Import(ClientTestConfig.class)
@WireMockTest
public class GithubClientServiceIntegrationTest {

    @Autowired
    private GithubClientService githubClientService;

    @Autowired
    private WireMockServer wireMockServer;


    @Test
    public void testGetAllInfo_Success() {
        wireMockServer.stubFor(get(urlEqualTo("/repos/owner/repo/commits"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"commit\":{\"message\":\"Test commit\",\"author\":{\"date\":\"2023-10-01T00:00:00Z\"}},\"author\":{\"login\":\"testUser\"}}]")));

        wireMockServer.stubFor(get(urlEqualTo("/repos/owner/repo/issues"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"title\":\"Test issue\",\"created_at\":\"2023-10-01T00:00:00Z\",\"user\":{\"login\":\"testUser\"}}]")));

        wireMockServer.stubFor(get(urlEqualTo("/repos/owner/repo/comments"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"body\":\"Test comment\",\"created_at\":\"2023-10-01T00:00:00Z\",\"user\":{\"login\":\"testUser\"}}]")));
        Link link = new Link("https://github.com/owner/repo", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

        List<UpdateInfo> result = githubClientService.getAllInfo(link);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testGetAllInfo_HttpClientErrorException() {
        wireMockServer.stubFor(get(urlEqualTo("/repos/owner/repo/commits"))
            .willReturn(aResponse().withStatus(404)));

        Link link = new Link("https://github.com/owner/repo", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

        assertThatThrownBy(() -> githubClientService.getAllInfo(link))
            .isInstanceOf(ScrapperInternalResponseException.class);
    }
}
