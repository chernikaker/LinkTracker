package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.scheduler.UpdateScheduler;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class TestClientConfig {

    @Bean("testGitHubClientService")
    @Primary
    public GithubClientService githubClientService() {
        GithubClientService mock = Mockito.mock(GithubClientService.class);
        Mockito.when(mock.isLinkAvailable(any(Link.class))).thenReturn(true);
        return mock;
    }

    @Bean("testSOClientService")
    @Primary
    public StackoverflowClientService soClientService() {
        StackoverflowClientService mock = Mockito.mock(StackoverflowClientService.class);
        Mockito.when(mock.isLinkAvailable(any(Link.class))).thenReturn(true);
        return mock;
    }

    @Bean("testBotClientService")
    @Primary
    public BotClientService botClientService() {
        return Mockito.mock(BotClientService.class);
    }

    @Bean("testScheduler")
    @Primary
    public UpdateScheduler schedulerUpdateService() {
        return Mockito.mock(UpdateScheduler.class);
    }

    @Bean("testBotClient")
    @Primary
    public BotClient botClient() {
        return Mockito.mock(BotClient.class);
    }

    @Bean("testExternalClient")
    @Primary
    public ExternalClient soClient() {
        return Mockito.mock(ExternalClient.class);
    }
}
