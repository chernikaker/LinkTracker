package backend.academy.scrapper.client;

import backend.academy.scrapper.client.github.GithubClient;
import backend.academy.scrapper.client.github.GithubClientImpl;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClient;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ClientTestConfig {

    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        WireMockServer s =  new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        s.start();
        return s;
    }

    @Bean
    @Primary
    public GithubClient githubClient(WireMockServer wireMockServer) {
        return new GithubClientImpl("test-token", "http://localhost:" + wireMockServer.port());
    }

    @Bean
    @Primary
    public StackoverflowClient stackoverflowClient(WireMockServer wireMockServer) {
        return new StackoverflowClientImpl("access-token", "key", "http://localhost:" + wireMockServer.port());
    }
}
