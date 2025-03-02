package backend.academy.scrapper.server;
import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.scheduler.SchedulerUpdateService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;


@TestConfiguration(proxyBeanMethods = false)
public class TestClientServiceConfig {

    @MockitoBean
    public GithubClientService githubClientService;

    @MockitoBean
    public StackoverflowClientService soClientService;

    @MockitoBean
    public BotClientService botClientService;

    @MockitoBean
    public SchedulerUpdateService schedulerUpdateService;


    @Bean
    @Primary
    public GithubClientService githubClientService() {
        GithubClientService mock = Mockito.mock(GithubClientService.class);
        Mockito.when(mock.isLinkAvailable(any(Link.class))).thenReturn(true);
        return mock;
    }

    @Bean
    @Primary
    public StackoverflowClientService soClientService() {
        StackoverflowClientService mock = Mockito.mock(StackoverflowClientService.class);
        Mockito.when(mock.isLinkAvailable(any(Link.class))).thenReturn(true);
        return mock;
    }

    @Bean
    @Primary
    public BotClientService botClientService() {
        return Mockito.mock(BotClientService.class);
    }

    @Bean
    @Primary
    public SchedulerUpdateService schedulerUpdateService() {
        return Mockito.mock(SchedulerUpdateService.class);
    }
}
