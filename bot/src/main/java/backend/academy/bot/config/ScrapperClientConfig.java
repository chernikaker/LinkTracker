package backend.academy.bot.config;

import backend.academy.bot.scrapperClient.ScrapperClient;
import backend.academy.bot.scrapperClient.ScrapperClientImpl;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

/**
 * Класс Spring конфгигурации для клиента Scrapper,предоставляет базовый URL Scrapper для отправки сообщений,
 * определенный в application.yaml
 *
 * @param baseUrl URL Scrapper
 */
@Validated
@ConfigurationProperties(prefix = "client", ignoreUnknownFields = false)
public record ScrapperClientConfig(@NotEmpty String baseUrl) {

    @Bean
    public ScrapperClient scrapperRestClient() {
        return new ScrapperClientImpl(baseUrl);
    }
}
