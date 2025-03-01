package backend.academy.bot.config;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.telegram.handler.HandlerService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {

    @Bean
    public String telegramToken() {
        return telegramToken;
    }

    @Bean
    public TelegramBotService telegramBot(HandlerService service) {
        return new TelegramBotService(telegramToken, service);
    }
}
