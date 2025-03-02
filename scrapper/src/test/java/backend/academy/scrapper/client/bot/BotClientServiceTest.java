package backend.academy.scrapper.client.bot;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.dto.UpdateInfoType;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BotClientServiceTest {

    @Mock
    private BotClient botClient;

    @Mock
    private InMemorySubscriptionRepository repository;

    @InjectMocks
    private BotClientService botClientService;

    private Link link;
    private Subscription subscription;
    private User user;
    private List<UpdateInfo> info;

    @BeforeEach
    public void setUp() {
        user = new User(1L);
        MockitoAnnotations.openMocks(this);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        link = new Link("http://github.com/mock", LinkType.GITHUB, now);
        subscription = new Subscription(1L, user, 1L, link, List.of(), List.of());
        info = new ArrayList<>();
        info.add(new UpdateInfo("type1", "author1", now, UpdateInfoType.COMMIT));
        info.add(new UpdateInfo("type2", "author2", now, UpdateInfoType.ISSUE));
    }

    @Test
    public void sendUpdates_success() {
        List<Subscription> subscriptions = List.of(subscription);
        when(repository.getLinkSubscriptions(link)).thenReturn(subscriptions);

        assertDoesNotThrow(() -> botClientService.sendUpdates(link, info));

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(botClient).sendUpdates(captor.capture());

        LinkUpdate capturedUpdate = captor.getValue();
        assertEquals(1, capturedUpdate.id());
        assertEquals("http://github.com/mock", capturedUpdate.url());
        assertTrue(capturedUpdate.description().contains("Тип сообщения: commit"));
        assertTrue(capturedUpdate.description().contains("Автор: author1"));
    }

    @Test
    public void sendUpdates_httpException() {
        List<Subscription> subscriptions = List.of(subscription);
        when(repository.getLinkSubscriptions(link)).thenReturn(subscriptions);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"))
            .when(botClient).sendUpdates(any(LinkUpdate.class));

        assertDoesNotThrow(() -> botClientService.sendUpdates(link, info));

        verify(botClient).sendUpdates(any(LinkUpdate.class));
    }
}
