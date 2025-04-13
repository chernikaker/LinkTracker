package backend.academy.scrapper.client.github;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.scrapper.client.WireMockClientTestConfig;
import backend.academy.scrapper.client.external.github.GithubClientService;
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

@SpringBootTest(classes = {GithubClientService.class})
@Import(WireMockClientTestConfig.class)
@WireMockTest
public class GithubClientServiceIntegrationTest {

    @Autowired
    private GithubClientService githubClientService;

    @Autowired
    private WireMockServer wireMockServer;

    @Test
    public void testGetAllInfo_Success() {
        setWireMockSuccessAnswerWithJson(
                "/repos/owner/repo/pulls",
                "[{\"title\":\"Pull request title\",\"user\":{\"login\":\"author1\"},\"created_at\":\"2023-10-01T12:00:00Z\", \"body\":\"body\"}]");
        setWireMockSuccessAnswerWithJson(
                "/repos/owner/repo/issues",
                "[{\"title\":\"Issue title\",\"user\":{\"login\":\"author2\"},\"created_at\":\"2023-10-01T12:00:00Z\", \"body\":\"body\"}]");

        Link link =
                new Link("https://github.com/owner/repo", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

        List<UpdateInfo> result = githubClientService.getAllInfo(link);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetAllInfo_HttpClientErrorException() {
        wireMockServer.stubFor(get(urlEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse().withStatus(404)));

        Link link =
                new Link("https://github.com/owner/repo", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

        assertThatThrownBy(() -> githubClientService.getAllInfo(link))
                .isInstanceOf(ScrapperInternalResponseException.class);
    }

    private void setWireMockSuccessAnswerWithJson(String request, String response) {
        wireMockServer.stubFor(get(urlEqualTo(request))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));
    }
}
