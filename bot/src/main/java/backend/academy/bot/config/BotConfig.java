package backend.academy.bot.config;

import backend.academy.bot.scrapperClient.ScrapperClient;
import backend.academy.bot.scrapperClient.ScrapperClientImpl;
import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.telegram.handler.HandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final BotPropertiesConfig properties;

    @Bean
    public TelegramBotService telegramBot(HandlerService service, ThreadPoolTaskExecutor ex) {
        return new TelegramBotService(properties.telegramToken(), service, ex);
    }

    @Bean
    public ScrapperClient scrapperRestClient() {
        return new ScrapperClientImpl(properties.scrapperBaseUrl());
    }

    @Bean
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Tg-Bot-Thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
