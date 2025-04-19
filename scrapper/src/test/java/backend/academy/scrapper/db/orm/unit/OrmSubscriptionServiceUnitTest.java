package backend.academy.scrapper.db.orm.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.orm.entity.OrmFilter;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.mapper.OrmFilterMapper;
import backend.academy.scrapper.db.orm.mapper.OrmLinkMapper;
import backend.academy.scrapper.db.orm.mapper.OrmTagMapper;
import backend.academy.scrapper.db.orm.mapper.OrmUserMapper;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.db.orm.service.OrmSubscriptionService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrmSubscriptionServiceUnitTest {

    private static final long ID = 1L;
    private static final String URL = "https://github.com/1";
    private static final String TAG_TEXT = "tag1";
    private static final String FILTER_KEY = "filter1";
    private static final String FILTER_VALUE = "value1";

    private static final User USER = new User(ID);
    private static final Link LINK = new Link(URL, LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
    private static final Tag TAG = new Tag(TAG_TEXT);
    private static final Filter FILTER = new Filter(FILTER_KEY, FILTER_VALUE);

    private static OrmUser ORM_USER = new OrmUser();
    private static OrmLink ORM_LINK = new OrmLink();
    private static OrmTag ORM_TAG = new OrmTag();
    private static OrmFilter ORM_FILTER = new OrmFilter();
    private static OrmSubscription ORM_SUBSCRIPTION = new OrmSubscription();

    @Mock
    private OrmSubscriptionRepository subscrRepo;

    @Mock
    private OrmLinkRepository linkRepo;

    @Mock
    private OrmUserRepository userRepo;

    @Mock
    private OrmTagRepository tagRepo;

    @InjectMocks
    private OrmSubscriptionService subscriptionService;

    @Test
    public void addSubscriptionOnLink_SuccessNewLink() {
        ORM_USER.subscriptions(new ArrayList<>());
        ORM_SUBSCRIPTION.id(ID);
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));
        when(linkRepo.findByUrl(URL)).thenReturn(Optional.empty());
        when(tagRepo.findByTagTextAndOwner(TAG_TEXT, ORM_USER)).thenReturn(Optional.empty());
        when(subscrRepo.save(any())).thenReturn(ORM_SUBSCRIPTION);

        long result = assertDoesNotThrow(
                () -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)));

        assertThat(result).isEqualTo(ID);
        verify(linkRepo).findByUrl(any());
        verify(linkRepo).save(any());
        verify(tagRepo).findByTagTextAndOwner(any(), any());
        verify(subscrRepo).save(any());
        verify(subscrRepo).flush();
    }

    @Test
    public void addSubscriptionOnLink_SuccessExistingLink() {
        ORM_USER.subscriptions(new ArrayList<>());
        ORM_SUBSCRIPTION.id(ID);
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));
        when(linkRepo.findByUrl(URL)).thenReturn(Optional.of(ORM_LINK));
        when(tagRepo.findByTagTextAndOwner(TAG_TEXT, ORM_USER)).thenReturn(Optional.empty());
        when(subscrRepo.save(any())).thenReturn(ORM_SUBSCRIPTION);

        assertDoesNotThrow(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)));

        verify(linkRepo, never()).save(any());
    }

    @Test
    public void addSubscriptionOnLink_SubscriptionAlreadyExists() {
        ORM_USER.subscriptions(List.of(ORM_SUBSCRIPTION));
        ORM_SUBSCRIPTION.link(ORM_LINK);
        ORM_LINK.url(URL);
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));
        when(linkRepo.findByUrl(URL)).thenReturn(Optional.of(ORM_LINK));

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)))
                .isInstanceOf(ScrapperSubscriptionAlreadyExistsException.class);
    }

    @Test
    public void addSubscriptionOnLink_UserNotExists() {
        when(userRepo.findByChatId(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void addSubscriptionOnLink_DataAccessException() {
        when(userRepo.findByChatId(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)))
                .isInstanceOf(ScrapperOrmException.class);
    }

    @Test
    public void getUserSubscriptions_Success() {
        ORM_LINK = OrmLinkMapper.mapToOrm(LINK);
        ORM_TAG = OrmTagMapper.mapToOrm(TAG);
        ORM_FILTER = OrmFilterMapper.mapToOrm(FILTER);
        ORM_USER = OrmUserMapper.mapToOrm(USER);
        ORM_SUBSCRIPTION = new OrmSubscription(ID, ORM_LINK, ORM_USER, List.of(ORM_FILTER), List.of(ORM_TAG));
        ORM_USER.subscriptions(List.of(ORM_SUBSCRIPTION));
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        Map<Long, Subscription> result = assertDoesNotThrow(() -> subscriptionService.getUserSubscriptions(USER));

        assertEquals(1, result.size());
        assertEquals(ID, result.entrySet().iterator().next().getKey());
        Subscription s = result.get(ID);
        assertEquals(LINK.url(), s.link().url());
        assertEquals(USER.chatId(), s.user().chatId());
        assertEquals(1, s.tags().size());
        assertEquals(1, s.filters().size());
    }

    @Test
    public void getUserSubscriptions_DataAccessException() {
        when(userRepo.findByChatId(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> subscriptionService.getUserSubscriptions(USER))
                .isInstanceOf(ScrapperOrmException.class);
    }

    @Test
    public void getSubscribersChatsByLinkId_LinkHasSubscribers() {
        ORM_LINK.subscriptions(List.of(ORM_SUBSCRIPTION));
        ORM_LINK.url(URL);
        ORM_USER.chatId(ID);
        ORM_SUBSCRIPTION.user(ORM_USER);
        ORM_SUBSCRIPTION.link(ORM_LINK);
        ORM_SUBSCRIPTION.tags(List.of(ORM_TAG));
        ORM_SUBSCRIPTION.filters(List.of(ORM_FILTER));
        when(linkRepo.findById(ID)).thenReturn(Optional.of(ORM_LINK));

        List<Subscription> result = assertDoesNotThrow(() -> subscriptionService.getSubscriptionsByLinkId(ID));

        assertEquals(1, result.size());
        Subscription s = result.getFirst();
        assertEquals(LINK.url(), s.link().url());
        assertEquals(USER.chatId(), s.user().chatId());
    }

    @Test
    public void getSubscribersChatsByLinkId_LinkDontHaveSubscribers() {
        ORM_LINK.subscriptions(List.of());
        when(linkRepo.findById(ID)).thenReturn(Optional.of(ORM_LINK));

        List<Subscription> result = assertDoesNotThrow(() -> subscriptionService.getSubscriptionsByLinkId(ID));

        assertTrue(result.isEmpty());
    }

    @Test
    public void getSubscribersChatsByLinkId_LinkNotFound() {
        when(linkRepo.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByLinkId(ID))
                .isInstanceOf(ScrapperLinkNotExistsException.class);
    }

    @Test
    public void getSubscribersChatsByLinkId_DataAccessException() {
        when(linkRepo.findById(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByLinkId(ID))
                .isInstanceOf(ScrapperOrmException.class);
    }

    @Test
    public void deleteSubscriptionByUserAndLink_SuccessUniqueLinkSub() {
        ORM_LINK = OrmLinkMapper.mapToOrm(LINK);
        ORM_TAG = OrmTagMapper.mapToOrm(TAG);
        ORM_FILTER = OrmFilterMapper.mapToOrm(FILTER);
        ORM_USER = OrmUserMapper.mapToOrm(USER);
        ORM_SUBSCRIPTION = new OrmSubscription(ID, ORM_LINK, ORM_USER, List.of(ORM_FILTER), List.of(ORM_TAG));
        ORM_USER.subscriptions(new ArrayList<>(List.of(ORM_SUBSCRIPTION)));
        ORM_LINK.subscriptions(new ArrayList<>(List.of(ORM_SUBSCRIPTION)));
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        var result = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK));

        assertEquals(ID, result.getKey());
        assertThat(result.getValue()).isNotNull();
        verify(subscrRepo).delete(ORM_SUBSCRIPTION);
        verify(linkRepo).delete(ORM_LINK);
        verify(subscrRepo).flush();
    }

    @Test
    public void deleteSubscriptionByUserAndLink_SuccessNotUniqueLink() {
        ORM_LINK = OrmLinkMapper.mapToOrm(LINK);
        ORM_TAG = OrmTagMapper.mapToOrm(TAG);
        ORM_FILTER = OrmFilterMapper.mapToOrm(FILTER);
        ORM_USER = OrmUserMapper.mapToOrm(USER);
        ORM_SUBSCRIPTION = new OrmSubscription(ID, ORM_LINK, ORM_USER, List.of(ORM_FILTER), List.of(ORM_TAG));
        ORM_USER.subscriptions(new ArrayList<>(List.of(ORM_SUBSCRIPTION)));
        ORM_LINK.subscriptions(new ArrayList<>(List.of(ORM_SUBSCRIPTION)));
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        var result = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK));

        assertEquals(ID, result.getKey());
        assertThat(result.getValue()).isNotNull();
        verify(subscrRepo).delete(ORM_SUBSCRIPTION);
        verify(linkRepo).delete(ORM_LINK);
        verify(subscrRepo).flush();
    }

    @Test
    public void deleteSubscriptionByUserAndLink_SubscriptionNotFound() {
        ORM_USER.subscriptions(List.of());
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionByUserAndLink_UserNotFound() {
        when(userRepo.findByChatId(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionByUserAndLink_DataAccessException() {
        when(userRepo.findByChatId(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK))
                .isInstanceOf(ScrapperOrmException.class);
    }
}
