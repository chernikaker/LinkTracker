package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Класс, инкапсулирующий данные из внешних конфигурационных файлов
 *
 * @param github параметры конфигурации клиента GitHub
 * @param stackOverflow параметры конфигурации клиента StackOverflow
 * @param botUrl URL для отправки запросов боту
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperPropertiesConfig(
        GithubCredentials github, StackOverflowCredentials stackOverflow, @NotEmpty String botUrl, SqlParams sql) {
    /**
     * параметры конфигурации клиента GitHub
     *
     * @param token токен для увеличения частоты запросов
     * @param baseUrl URL для отправки запросов GitHub
     */
    public record GithubCredentials(@NotEmpty String token, @NotEmpty String baseUrl) {}

    /**
     * параметры конфигурации клиента StackOverflow
     *
     * @param key токен для увеличения частоты запросов
     * @param accessToken токен для доступа к методам API
     * @param baseUrl URL для отправки запросов StackOverflow
     */
    public record StackOverflowCredentials(
            @NotEmpty String key, @NotEmpty String accessToken, @NotEmpty String baseUrl) {}

    public record SqlParams(int batchSize, long secondsCheck) {}
}
