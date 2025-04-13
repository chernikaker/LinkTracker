package backend.academy.scrapper.client.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
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

public class BotClientServiceTest {

    @Mock
    private BotClient botClient;

    @Mock
    private SubscriptionService subscriptionService;

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
        info = new ArrayList<>();
        info.add(new UpdateInfo("title1", "message1", "author1", now, UpdateInfoType.PULL_REQUEST));
        info.add(new UpdateInfo("title2", "message2", "author2", now, UpdateInfoType.ISSUE));
    }

    @Test
    public void sendUpdates_success() {
        when(subscriptionService.getSubscribersChatsByLinkId(1L)).thenReturn(List.of(1L, 2L));

        assertDoesNotThrow(() -> botClientService.sendUpdates(1L, link.url(), info));

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(botClient).sendUpdates(captor.capture());

        LinkUpdate capturedUpdate = captor.getValue();
        assertEquals(1L, capturedUpdate.id());
        assertEquals("http://github.com/mock", capturedUpdate.url());
        assertThat(capturedUpdate.tgChatIds()).contains(1L, 2L);
        List<LinkUpdateUnit> units = capturedUpdate.updateUnits();
        assertEquals(2, units.size());
        assertEquals("message1", units.getFirst().description());
        assertEquals("message2", units.get(1).description());
    }

    @Test
    public void sendUpdates_httpException() {
        when(subscriptionService.getSubscribersChatsByLinkId(1L)).thenReturn(List.of(1L, 2L));
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"))
                .when(botClient)
                .sendUpdates(any(LinkUpdate.class));

        assertDoesNotThrow(() -> botClientService.sendUpdates(1L, link.url(), info));

        verify(botClient).sendUpdates(any(LinkUpdate.class));
    }
}
