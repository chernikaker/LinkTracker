package backend.academy.scrapper.config;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientImpl;
import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.client.external.github.GithubClientImpl;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientImpl;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.scheduler.UpdateScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Класс Spring конфигурации для внешних клиентов */
@Configuration
@RequiredArgsConstructor
public class ScrapperConfig {

    /** Record, инкапсулирующий данные из внешних конфигурационных файлов */
    private final ScrapperPropertiesConfig properties;

    @Bean
    public ExternalClient githubClient() {
        return new GithubClientImpl(
                properties.github().token(), properties.github().baseUrl());
    }

    @Bean
    public BotClient botClient() {
        return new BotClientImpl(properties.botUrl());
    }

    @Bean
    public ExternalClient stackoverflowClient() {
        return new StackoverflowClientImpl(
                properties.stackOverflow().accessToken(),
                properties.stackOverflow().key(),
                properties.stackOverflow().baseUrl());
    }

    @Bean
    public UpdateScheduler updateScheduler(
        LinkService linkDbService,
        GithubClientService githubClientService,
        StackoverflowClientService soClientService,
        BotClientService botClientService
    ) {
        return new UpdateScheduler(
            linkDbService,
            githubClientService,
            soClientService,
            botClientService,
            properties.sql().secondsCheck(),
            properties.sql().batchSize()
        );
    }
}
