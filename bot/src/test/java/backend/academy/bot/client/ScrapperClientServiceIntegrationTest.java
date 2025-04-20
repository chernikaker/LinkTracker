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

import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import backend.academy.dto.ListTagsResponse;
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

    public static final String ADD_LINK_REQUEST_BODY =
            """
    {
      "link": "https://example.com",
      "tags": ["tag1", "tag2"],
      "filters": ["filter1"]
    }
    """;
    public static final String UNTRACK_REQUEST_BODY = """
    {
        "link": "https://example.com"
    }
    """;

    public static final String ADD_TAGS_TO_SUB_REQUEST =
            """
    {
        "link": "https://example.com",
        "tags": ["tag1"]
    }
    """;

    public static final String REMOVE_TAG_FROM_SUB_REQUEST =
            """
    {
        "link": "https://example.com",
        "tag": "tag1"
    }
    """;

    public static final String DELETE_TAG_REQUEST = """
    {
        "tag": "tag"
    }
    """;

    private static final String ERROR_RESPONSE_BODY =
            """
    {
        "description": "Invalid request",
        "code": "400",
        "exceptionName": "BadRequestException",
        "exceptionMessage": "Invalid chat ID",
        "stackTrace": []
    }
    """;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private ScrapperClientService service;

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
                        .withBody(ERROR_RESPONSE_BODY)));

        assertThatThrownBy(() -> service.registerNewClient(123)).isInstanceOf(BotRequestException.class);
    }

    @Test
    public void getUserLinks_scrapperReturnsError() {
        wireMockServer.stubFor(get(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));

        assertThatThrownBy(() -> service.getUserLinks(123)).isInstanceOf(BotRequestException.class);
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
        String responseBody =
                """
            {
                "id": 1,
                "url": "https://example.com"
            }
            """;
        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(ADD_LINK_REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com",
                new String[] {"tag1", "tag2"},
                new String[] {"filter1"},
                UserState.DEFAULT,
                Command.TRACK);
        assertDoesNotThrow(() -> service.addLinkSubscription(123L, request));
    }

    @Test
    public void addLinkSubscription_scrapperReturnsError() {
        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(ADD_LINK_REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com",
                new String[] {"tag1", "tag2"},
                new String[] {"filter1"},
                UserState.DEFAULT,
                Command.TRACK);
        assertThatThrownBy(() -> service.addLinkSubscription(123, request)).isInstanceOf(BotRequestException.class);
    }

    @Test
    public void deleteLinkSubscription_success() {
        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(UNTRACK_REQUEST_BODY))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        assertDoesNotThrow(() -> service.removeLinkSubscription(123L, "https://example.com"));
    }

    @Test
    public void deleteLinkSubscription_ScrapperReturnsError() {
        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(UNTRACK_REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));

        assertThatThrownBy(() -> service.removeLinkSubscription(123L, "https://example.com"))
                .isInstanceOf(BotRequestException.class);
    }

    @Test
    public void addTagsToSubscription_success() {
        String responseBody =
                """
        {
            "url": "https://example.com",
            "tags":{
                "tags": [{"id": 1, "value":"tag"}],
                "size": 1
            }
        }
        """;
        wireMockServer.stubFor(post(urlEqualTo("/links/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(ADD_TAGS_TO_SUB_REQUEST))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com",
                new String[] {"tag1"},
                new String[0],
                UserState.TRACKING_TAG,
                Command.TAGS_TO_SUB);
        assertDoesNotThrow(() -> service.addTagsToSubscription(123L, request));
    }

    @Test
    public void addTagsToSubscription_scrapperReturnsException() {
        wireMockServer.stubFor(post(urlEqualTo("/links/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(ADD_TAGS_TO_SUB_REQUEST))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com",
                new String[] {"tag1"},
                new String[0],
                UserState.TRACKING_TAG,
                Command.TAGS_TO_SUB);
        assertThatThrownBy(() -> service.addTagsToSubscription(123L, request)).isInstanceOf(BotRequestException.class);
    }

    @Test
    public void removeTagFromSubscription_success() {
        String responseBody =
                """
        {
            "tag":{
                "id": 1,
                "value":"tag"
            },
            "url": "https://example.com"
        }
        """;
        wireMockServer.stubFor(delete(urlEqualTo("/links/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(REMOVE_TAG_FROM_SUB_REQUEST))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com",
                new String[] {"tag1"},
                new String[0],
                UserState.TRACKING_TAG,
                Command.REMOVE_TAG_SUB);
        assertDoesNotThrow(() -> service.removeTagForSubscription(123L, request));
    }

    @Test
    public void removeTagFromSubscription_ScrapperReturnsException() {
        wireMockServer.stubFor(delete(urlEqualTo("/links/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(REMOVE_TAG_FROM_SUB_REQUEST))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));

        LinkTrackingObject request = new LinkTrackingObject(
                "https://example.com",
                new String[] {"tag1"},
                new String[0],
                UserState.TRACKING_TAG,
                Command.REMOVE_TAG_SUB);
        assertThatThrownBy(() -> service.removeTagForSubscription(123L, request))
                .isInstanceOf(BotRequestException.class);
    }

    @Test
    public void removeSubscriptionsByTag_success() {
        String tag = "tag";
        String responseBody =
                """
        {
            "tag":"tag",
            "links":{
                "links":[{"id":1, "url":"https://example.com", "tags":["tag"],"filters":[]}],
                "size":1
            }
        }
        """;
        wireMockServer.stubFor(delete(urlEqualTo("/tags/" + tag + "/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        assertDoesNotThrow(() -> service.removeSubscriptionsByTag(123L, tag));
    }

    @Test
    public void removeSubscriptionsByTag_ScrapperReturnsException() {
        String tag = "tag";
        wireMockServer.stubFor(delete(urlEqualTo("/tags/" + tag + "/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));
        assertThatThrownBy(() -> service.removeSubscriptionsByTag(123L, tag)).isInstanceOf(BotRequestException.class);
    }

    @Test
    public void deleteTag_success() {
        String tag = "tag";
        String responseBody = """
        {
            "id":1,
            "value":"tag"
        }
        """;
        wireMockServer.stubFor(delete(urlEqualTo("/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(DELETE_TAG_REQUEST))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        assertDoesNotThrow(() -> service.deleteTag(123L, tag));
    }

    @Test
    public void deleteTag_ScrapperReturnsException() {
        String tag = "tag";
        wireMockServer.stubFor(delete(urlEqualTo("/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .withRequestBody(equalToJson(DELETE_TAG_REQUEST))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));
        assertThatThrownBy(() -> service.deleteTag(123L, tag)).isInstanceOf(BotRequestException.class);
    }

    @Test
    public void getSubscriptionsByTag_success() {
        String tag = "tag";
        String responseBody =
                """
        {
            "tag":"tag",
            "links":{
                "links":[{"id":1, "url":"https://example.com", "tags":["tag"],"filters":[]}],
                "size":1
            }
        }
        """;
        wireMockServer.stubFor(get(urlEqualTo("/tags/" + tag + "/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        ListTagLinksResponse r = assertDoesNotThrow(() -> service.getSubscriptionsByTag(123L, tag));
        assertEquals(tag, r.tag());
        assertEquals(1, r.links().size());
    }

    @Test
    public void getSubscriptionsByTag_ScrapperReturnsException() {
        String tag = "tag";
        wireMockServer.stubFor(get(urlEqualTo("/tags/" + tag + "/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));
        assertThatThrownBy(() -> service.getSubscriptionsByTag(123L, tag)).isInstanceOf(BotRequestException.class);
    }

    @Test
    public void getTags_success() {
        String responseBody =
                """
        {
            "tags": [{"id": 1, "value":"tag"}],
            "size": 1
        }
        """;
        wireMockServer.stubFor(get(urlEqualTo("/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        ListTagsResponse r = assertDoesNotThrow(() -> service.getUserTags(123L));
        assertEquals(1, r.size());
        assertEquals("tag", r.tags().getFirst().value());
    }

    @Test
    public void getTags_ScrapperReturnsException() {
        wireMockServer.stubFor(get(urlEqualTo("/tags"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_RESPONSE_BODY)));
        assertThatThrownBy(() -> service.getUserTags(123L)).isInstanceOf(BotRequestException.class);
    }
}
