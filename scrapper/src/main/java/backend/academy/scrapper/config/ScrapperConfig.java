package backend.academy.scrapper.config;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientImpl;
import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.client.external.github.GithubClientImpl;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientImpl;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

/**
 * Класс Spring конфигурации для внешних клиентов
 *
 * @param github параметры конфигурации клиента GitHub
 * @param stackOverflow параметры конфигурации клиента StackOverflow
 * @param botUrl URL для отправки запросов боту
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        GithubCredentials github, StackOverflowCredentials stackOverflow, @NotEmpty String botUrl) {
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

    @Bean
    public ExternalClient githubClient() {
        return new GithubClientImpl(github.token(), github.baseUrl);
    }

    @Bean
    public BotClient botClient() {
        return new BotClientImpl(botUrl);
    }

    @Bean
    public ExternalClient stackoverflowClient() {
        return new StackoverflowClientImpl(stackOverflow.accessToken, stackOverflow.key, stackOverflow.baseUrl);
    }
}
