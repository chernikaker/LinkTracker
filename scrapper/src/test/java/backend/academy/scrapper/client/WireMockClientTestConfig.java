package backend.academy.scrapper.client;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientImpl;
import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.client.external.github.GithubClientImpl;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class WireMockClientTestConfig {

    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        WireMockServer s =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        s.start();
        return s;
    }

    @Bean
    @Primary
    public ExternalClient githubClient(WireMockServer wireMockServer) {
        return new GithubClientImpl("test-token", "http://localhost:" + wireMockServer.port());
    }

    @Bean
    @Primary
    public ExternalClient stackoverflowClient(WireMockServer wireMockServer) {
        return new StackoverflowClientImpl("access-token", "key", "http://localhost:" + wireMockServer.port());
    }

    @Bean
    @Primary
    public BotClient botClient(WireMockServer wireMockServer) {
        return new BotClientImpl("http://localhost:" + wireMockServer.port());
    }
}
