package backend.academy.bot.config;

import backend.academy.bot.scrapperClient.IClient;
import backend.academy.bot.scrapperClient.ScrapperRestClient;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "client", ignoreUnknownFields = false)
public record ScrapperClientConfig (@NotEmpty String baseUrl) {

    @Bean
    public IClient scrapperRestClient() {
        return new ScrapperRestClient(baseUrl);
    }
}
