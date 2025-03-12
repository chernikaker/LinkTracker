package backend.academy.bot.config;

import backend.academy.bot.scrapperClient.ScrapperClient;
import backend.academy.bot.scrapperClient.ScrapperClientImpl;
import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.telegram.handler.HandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final BotPropertiesConfig properties;

    @Bean
    public TelegramBotService telegramBot(HandlerService service) {
        return new TelegramBotService(properties.telegramToken(), service);
    }

    @Bean
    public ScrapperClient scrapperRestClient() {
        return new ScrapperClientImpl(properties.scrapperBaseUrl());
    }
}
