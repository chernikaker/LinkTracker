package backend.academy.scrapper.client.bot;

import backend.academy.scrapper.client.WireMockClientTestConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.db.config.SqlConfig;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@JdbcTest
@WireMockTest
@Import({BotClientService.class, SqlConfig.class, WireMockClientTestConfig.class})
@Testcontainers
@Transactional
public class BotClientServiceIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.now(ZoneId.systemDefault());
    private static final String URL = "https://github.com/owner/repo";
    private static final List<UpdateInfo> updates = List.of(
        new UpdateInfo("title1", "message1", "author1", NOW, UpdateInfoType.PULL_REQUEST),
        new UpdateInfo("title2","message2", "author2", NOW, UpdateInfoType.ISSUE)
    );

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private BotClientService botClientService;

    @Autowired
    private WireMockServer wireMockServer;

    @Test
    public void testSendUpdates_Success() {
        Long linkId = setUpLinkSubscriptions();
        wireMockServer.stubFor(
                post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        assertDoesNotThrow(() -> botClientService.sendUpdates(linkId, URL, updates));

        wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void testSendUpdates_HttpClientErrorException() {
        Long linkId = setUpLinkSubscriptions();
        wireMockServer.stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBody("{\"exceptionMessage\":\"Invalid request\"}")));

        assertDoesNotThrow(() -> botClientService.sendUpdates(linkId, URL, updates));

        wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
    }

    private Long setUpLinkSubscriptions(){
        Long linkId = jdbc.queryForObject("INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
            Long.class,URL, NOW);
        Long userId = jdbc.queryForObject("INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id",Long.class,1L);
        jdbc.update("INSERT INTO subscription (link_id, user_id) VALUES (?, ?)", linkId, userId);
        return linkId;
    }
}
