package backend.academy.scrapper.config;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientImpl;
import backend.academy.scrapper.client.github.GithubClient;
import backend.academy.scrapper.client.github.GithubClientImpl;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClient;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientImpl;
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
    public GithubClient githubClient() {
        return new GithubClientImpl(github.token(), github.baseUrl);
    }

    @Bean
    public BotClient botClient() {
        return new BotClientImpl(botUrl);
    }

    @Bean
    public StackoverflowClient stackoverflowClient() {
        return new StackoverflowClientImpl(stackOverflow.accessToken, stackOverflow.key, stackOverflow.baseUrl);
    }
}
