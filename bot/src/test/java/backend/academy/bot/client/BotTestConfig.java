package backend.academy.bot.client;

import backend.academy.bot.scrapperClient.ScrapperClient;
import backend.academy.bot.scrapperClient.ScrapperClientImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class BotTestConfig {

    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        WireMockServer s =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        s.start();
        return s;
    }

    @Bean("testClient")
    @Primary
    public ScrapperClient scrapperClient(WireMockServer wireMockServer) {
        return new ScrapperClientImpl("http://localhost:" + wireMockServer.port());
    }
}
