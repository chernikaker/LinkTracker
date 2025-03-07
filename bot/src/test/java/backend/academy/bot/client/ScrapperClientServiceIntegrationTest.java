package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.bot.exception.scrapperClient.BotChatRegistrationException;
import backend.academy.bot.exception.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ListLinksResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@SpringBootTest(classes = {ScrapperClientService.class})
@Import(BotTestConfig.class)
@WireMockTest
public class ScrapperClientServiceIntegrationTest {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private ScrapperClientService service;

    private final String errorResponseBody =
            """
        {
            "description": "Invalid request",
            "code": "400",
            "exceptionName": "BadRequestException",
            "exceptionMessage": "Invalid chat ID",
            "stackTrace": []
        }
        """;

    @Test
    public void registerChat_success() {
        wireMockServer.stubFor(
                post(urlEqualTo("/tg-chat/123")).willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        assertDoesNotThrow(() -> service.registerNewClient(123L));

        wireMockServer.verify(postRequestedFor(urlEqualTo("/tg-chat/123")));
    }

    @Test
    public void registerChat_scrapperReturnsError() {
        wireMockServer.stubFor(post(urlEqualTo("/tg-chat/123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponseBody)));

        assertThatThrownBy(() -> service.registerNewClient(123)).isInstanceOf(BotChatRegistrationException.class);
    }

    @Test
    public void getUserLinks_scrapperReturnsError() {
        wireMockServer.stubFor(get(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponseBody)));

        assertThatThrownBy(() -> service.getUserLinks(123)).isInstanceOf(BotInvalidLinkRequestException.class);
    }

    @Test
    public void getUserLinks_success() {
        String responseBody =
                """
        {
            "links": [
                {"id": 1, "url": "https://example.com"},
                {"id": 2, "url": "https://example.org"}
            ],
            "size": 2
        }
        """;
        wireMockServer.stubFor(get(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        ListLinksResponse response = assertDoesNotThrow(() -> service.getUserLinks(123L));

        assertEquals(2, response.size());
        assertEquals("https://example.com", response.links().getFirst().url());
    }

    @Test
    public void addLinkSubscription_success() {
        String requestBody =
                """
            {
                "link": "https://example.com",
                "tags": ["tag1", "tag2"],
                "filters": ["filter1"]
            }
            """;
        String responseBody =
                """
            {
                "id": 1,
                "url": "https://example.com"
            }
            """;
        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1", "tag2"}, new String[] {"filter1"}, UserState.DEFAULT);
        assertDoesNotThrow(() -> service.addLinkSubscription(123L, request));
    }

    @Test
    public void addLinkSubscription_scrapperReturnsError() {
        String requestBody =
                """
            {
                "link": "https://example.com",
                "tags": ["tag1", "tag2"],
                "filters": ["filter1"]
            }
            """;
        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponseBody)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1", "tag2"}, new String[] {"filter1"}, UserState.DEFAULT);
        assertThatThrownBy(() -> service.addLinkSubscription(123, request))
                .isInstanceOf(BotInvalidLinkRequestException.class);
    }

    @Test
    public void deleteLinkSubscription_success() {
        String requestBody = """
        {
            "link": "https://example.com"
        }
        """;
        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        assertDoesNotThrow(() -> service.removeLinkSubscription(123L, "https://example.com"));
    }

    @Test
    public void deleteLinkSubscription_ScrapperReturnsError() {
        String requestBody = """
        {
            "link": "https://example.com"
        }
        """;
        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponseBody)));

        assertThatThrownBy(() -> service.removeLinkSubscription(123L, "https://example.com"))
                .isInstanceOf(BotInvalidLinkRequestException.class);
    }
}
