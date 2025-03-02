package backend.academy.scrapper.server;

import static org.mockito.ArgumentMatchers.any;

import backend.academy.scrapper.client.bot.BotClient;
import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.github.GithubClient;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClient;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.scheduler.SchedulerUpdateService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration(proxyBeanMethods = false)
public class TestClientConfig {

    @MockitoBean
    private GithubClientService githubClientService;

    @MockitoBean
    private StackoverflowClientService soClientService;

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
    public SchedulerUpdateService schedulerUpdateService() {
        return Mockito.mock(SchedulerUpdateService.class);
    }

    @Bean("testBotClient")
    @Primary
    public BotClient botClient() {
        return Mockito.mock(BotClient.class);
    }

    @Bean("testSOClient")
    @Primary
    public StackoverflowClient soClient() {
        return Mockito.mock(StackoverflowClient.class);
    }

    @Bean("testGithubClient")
    @Primary
    public GithubClient gitClient() {
        return Mockito.mock(GithubClient.class);
    }
}
