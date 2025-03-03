package backend.academy.bot.config;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.telegram.handler.HandlerService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

/**
 * Класс Spring конфгигурации для телеграм-бота, предоставляет токен для его работы, определенный в application.yaml
 *
 * @param telegramToken токен телеграм-бота
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {

    @Bean
    public TelegramBotService telegramBot(HandlerService service) {
        return new TelegramBotService(telegramToken, service);
    }
}
