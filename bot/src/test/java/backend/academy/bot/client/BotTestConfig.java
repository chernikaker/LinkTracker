package backend.academy.bot.client;

import backend.academy.bot.scrapperClient.IClient;
import backend.academy.bot.scrapperClient.ScrapperRestClient;
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
    public IClient scrapperClient(WireMockServer wireMockServer) {
        return new ScrapperRestClient("http://localhost:" + wireMockServer.port());
    }
}
