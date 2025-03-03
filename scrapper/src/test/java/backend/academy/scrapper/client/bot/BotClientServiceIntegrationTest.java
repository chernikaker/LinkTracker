package backend.academy.scrapper.client.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.WireMockClientTestConfig;
import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.dto.UpdateInfoType;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {BotClientService.class, InMemorySubscriptionRepository.class})
@Import(WireMockClientTestConfig.class)
@WireMockTest
public class BotClientServiceIntegrationTest {

    @Autowired
    private BotClientService botClientService;

    @MockitoBean
    private InMemorySubscriptionRepository repository;

    @Autowired
    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        repository.clear();
    }

    @Test
    public void testSendUpdates_Success() {
        wireMockServer.stubFor(
                post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        Link link = new Link("http://example.com", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        Subscription subscription = new Subscription(1L, null, 1L, link, List.of(), List.of());
        when(repository.getLinkSubscriptions(link)).thenReturn(List.of(subscription));
        List<UpdateInfo> updates = List.of(
                new UpdateInfo("commit", "author1", LocalDateTime.now(ZoneId.systemDefault()), UpdateInfoType.COMMIT),
                new UpdateInfo("issue", "author2", LocalDateTime.now(ZoneId.systemDefault()), UpdateInfoType.ISSUE));

        botClientService.sendUpdates(link, updates);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void testSendUpdates_HttpClientErrorException() {

        wireMockServer.stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBody("{\"exceptionMessage\":\"Invalid request\"}")));
        Link link = new Link("http://example.com", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        Subscription subscription = new Subscription(1L, null, 1L, link, List.of(), List.of());
        when(repository.getLinkSubscriptions(link)).thenReturn(List.of(subscription));
        List<UpdateInfo> updates = List.of(
                new UpdateInfo("commit", "author1", LocalDateTime.now(ZoneId.systemDefault()), UpdateInfoType.COMMIT),
                new UpdateInfo("issue", "author2", LocalDateTime.now(ZoneId.systemDefault()), UpdateInfoType.ISSUE));

        assertDoesNotThrow(() -> botClientService.sendUpdates(link, updates));

        wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
    }
}
