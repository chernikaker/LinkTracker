package backend.academy.scrapper.config;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientImpl;
import backend.academy.scrapper.client.github.GithubClient;
import backend.academy.scrapper.client.github.GithubClientImpl;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    GithubCredentials github,
    StackOverflowCredentials stackOverflow,
    @NotEmpty String botUrl
) {
    public record GithubCredentials(
        @NotEmpty String token,
        @NotEmpty String baseUrl
    ) { }

    public record StackOverflowCredentials(
        @NotEmpty String key,
        @NotEmpty String accessToken
    ) { }

    @Bean
    public GithubClient githubClient() {
        return new GithubClientImpl(github.token(), github.baseUrl);
    }

    @Bean
    public BotClient botClient() {
        return new BotClientImpl(botUrl);
    }
}
