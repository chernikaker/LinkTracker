package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Класс Spring конфгигурации для телеграм-бота, предоставляет токен для его работы, определенный в application.yaml
 *
 * @param telegramToken токен телеграм-бота
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotPropertiesConfig(@NotEmpty String telegramToken, @NotEmpty String scrapperBaseUrl) {}
