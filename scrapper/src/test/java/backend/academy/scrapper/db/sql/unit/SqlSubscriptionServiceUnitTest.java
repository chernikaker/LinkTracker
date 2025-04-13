package backend.academy.scrapper.db.sql.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.sql.entity.SqlFilter;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.entity.SqlTag;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.FilterSqlRepository;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.TagSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.db.sql.service.SqlSubscriptionService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SqlSubscriptionServiceUnitTest {

    @Mock
    private UserSqlRepository userRepo;

    @Mock
    private LinkSqlRepository linkRepo;

    @Mock
    private SubscriptionSqlRepository subscrRepo;

    @Mock
    private TagSqlRepository tagRepo;

    @Mock
    private FilterSqlRepository filterRepo;

    @InjectMocks
    private SqlSubscriptionService subscriptionService;

    private static User testUser;
    private static Link testLink;
    private static List<Tag> testTags;
    private static List<Filter> testFilters;
    private static SqlUser sqlUser;
    private static SqlTag sqlTag;
    private static SqlLink sqlLink;
    private static SqlSubscription sqlSubscription;
    private static SqlFilter sqlFilter;

    @BeforeAll
    public static void setUp() {
        testUser = new User(1L);
        testLink = new Link("https://github.com/1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        testTags = List.of(new Tag("java"));
        testFilters = List.of(new Filter("lang", "en"));
        sqlUser = new SqlUser(1L, 1L);
        sqlTag = new SqlTag(1L, "java", 1L);
        sqlLink = new SqlLink(1L, testLink.url(), testLink.lastValidation());
        sqlSubscription = new SqlSubscription(1L, 1L, 1L);
        sqlFilter = new SqlFilter(1L, "lang", "en", 1L, 1L);
    }

    @Test
    public void addSubscriptionOnLink_SuccessNewTag() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(testLink.url())).thenReturn(Optional.empty());
        when(linkRepo.addLink(any())).thenReturn(1L);
        when(subscrRepo.addSubscription(any())).thenReturn(1L);
        when(tagRepo.getTagByTextAndUserId(eq(1L), any())).thenReturn(Optional.empty());
        when(tagRepo.addTag(any())).thenReturn(1L);

        long subscriptionId = assertDoesNotThrow(
                () -> subscriptionService.addSubscriptionOnLink(testUser, testLink, testTags, testFilters));

        assertEquals(1L, subscriptionId);
        verify(userRepo).findUserByChatId(testUser.chatId());
        verify(linkRepo).findLinkByUrl(testLink.url());
        verify(subscrRepo).addSubscription(any());
        verify(tagRepo).addTag(any());
        verify(filterRepo).addFilterToSubscription(any());
    }

    @Test
    public void addSubscriptionOnLink_SuccessExistingTag() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(testLink.url())).thenReturn(Optional.empty());
        when(linkRepo.addLink(any())).thenReturn(1L);
        when(subscrRepo.addSubscription(any())).thenReturn(1L);
        when(tagRepo.getTagByTextAndUserId(eq(1L), any())).thenReturn(Optional.of(sqlTag));

        long subscriptionId = assertDoesNotThrow(
                () -> subscriptionService.addSubscriptionOnLink(testUser, testLink, testTags, testFilters));

        assertEquals(1L, subscriptionId);
        verify(userRepo).findUserByChatId(testUser.chatId());
        verify(linkRepo).findLinkByUrl(testLink.url());
        verify(subscrRepo).addSubscription(any());
        verify(tagRepo, never()).addTag(any());
        verify(filterRepo).addFilterToSubscription(any());
    }

    @Test
    public void addSubscriptionOnLink_SuccessExistingLink() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(testLink.url())).thenReturn(Optional.of(sqlLink));
        when(subscrRepo.getSubscriptionByLinkAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(subscrRepo.addSubscription(any())).thenReturn(1L);
        when(tagRepo.getTagByTextAndUserId(eq(1L), any())).thenReturn(Optional.empty());
        when(tagRepo.addTag(any())).thenReturn(1L);

        long subscriptionId = assertDoesNotThrow(
                () -> subscriptionService.addSubscriptionOnLink(testUser, testLink, testTags, testFilters));

        assertEquals(1L, subscriptionId);
        verify(userRepo).findUserByChatId(testUser.chatId());
        verify(linkRepo).findLinkByUrl(testLink.url());
        verify(subscrRepo).getSubscriptionByLinkAndUserId(1L, 1L);
        verify(subscrRepo).addSubscription(any());
        verify(tagRepo).addTag(any());
        verify(filterRepo).addFilterToSubscription(any());
    }

    @Test
    public void addSubscriptionOnLink_UserNotExists() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(testUser, testLink, testTags, testFilters))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void addSubscriptionOnLink_SubscriptionAlreadyExists() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(testLink.url())).thenReturn(Optional.of(sqlLink));
        when(subscrRepo.getSubscriptionByLinkAndUserId(1L, 1L)).thenReturn(Optional.of(sqlSubscription));

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(testUser, testLink, testTags, testFilters))
                .isInstanceOf(ScrapperSubscriptionAlreadyExistsException.class);
    }

    @Test
    void addSubscriptionOnLink_DataAccessException() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenThrow(new TestDataAccessException("DB error"));

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(testUser, testLink, testTags, testFilters))
                .isInstanceOf(ScrapperSqlException.class);
    }

    @Test
    void getUserSubscriptions_Success() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(subscrRepo.getSubscriptionsByUserId(1L)).thenReturn(List.of(sqlSubscription));
        when(linkRepo.getLinkById(1L)).thenReturn(sqlLink);
        when(tagRepo.getSubscriptionTags(1L)).thenReturn(List.of(sqlTag));
        when(filterRepo.getFiltersBySubscriptionId(1L)).thenReturn(List.of(sqlFilter));

        Map<Long, Subscription> subscriptions =
                assertDoesNotThrow(() -> subscriptionService.getUserSubscriptions(testUser));

        assertEquals(1, subscriptions.size());
        Subscription subscription = subscriptions.get(1L);
        assertEquals(testUser, subscription.user());
        assertEquals(testLink.url(), subscription.link().url());
        assertEquals(1, subscription.tags().size());
        assertEquals(1, subscription.filters().size());
    }

    @Test
    void getUserSubscriptions_NoSubscriptions() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(subscrRepo.getSubscriptionsByUserId(1L)).thenReturn(List.of());

        Map<Long, Subscription> subscriptions =
                assertDoesNotThrow(() -> subscriptionService.getUserSubscriptions(testUser));

        assertTrue(subscriptions.isEmpty());
    }

    @Test
    void getUserSubscriptions_UserNotFound() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getUserSubscriptions(testUser))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    void getUserSubscriptions_DataAccessException() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> subscriptionService.getUserSubscriptions(testUser))
                .isInstanceOf(ScrapperSqlException.class);
    }

    @Test
    public void getSubscribersChatsByLinkId_ReturnsChatIds() {
        when(subscrRepo.getChatsByLink(1L)).thenReturn(List.of(1L));

        List<Long> result = assertDoesNotThrow(() -> subscriptionService.getSubscribersChatsByLinkId(1L));

        assertThat(result).containsExactly(1L);
        verify(subscrRepo).getChatsByLink(1L);
    }

    @Test
    public void getSubscribersChatsByLinkId_NoSubscribers() {
        long linkId = 1L;
        when(subscrRepo.getChatsByLink(linkId)).thenReturn(List.of());

        List<Long> result = assertDoesNotThrow(() -> subscriptionService.getSubscribersChatsByLinkId(linkId));

        assertThat(result).isEmpty();
        verify(subscrRepo).getChatsByLink(linkId);
    }

    @Test
    public void getSubscribersChatsByLinkId_DataAccessException() {
        long linkId = 1L;
        when(subscrRepo.getChatsByLink(linkId)).thenThrow(new TestDataAccessException("DB error"));

        assertThatThrownBy(() -> subscriptionService.getSubscribersChatsByLinkId(linkId))
                .isInstanceOf(ScrapperSqlException.class);
    }

    @Test
    void deleteSubscriptionByUserAndLink_Success() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(testLink.url())).thenReturn(Optional.of(sqlLink));
        when(subscrRepo.getSubscriptionByLinkAndUserId(1L, 1L)).thenReturn(Optional.of(sqlSubscription));
        when(tagRepo.getSubscriptionTags(1L)).thenReturn(List.of(sqlTag));
        when(filterRepo.getFiltersBySubscriptionId(1L)).thenReturn(List.of(sqlFilter));

        var result = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionByUserAndLink(testUser, testLink));

        assertEquals(1L, result.getKey());
        assertEquals(testUser, result.getValue().user());
        verify(subscrRepo).deleteSubscriptionById(1L);
    }

    @Test
    void deleteSubscriptionByUserAndLink_UserNotExists() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(testUser, testLink))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    void deleteSubscriptionByUserAndLink_SubscriptionNotExists() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(testLink.url())).thenReturn(Optional.of(sqlLink));
        when(subscrRepo.getSubscriptionByLinkAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(testUser, testLink))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    void deleteSubscriptionByUserAndLink_DataAccessException() {
        when(userRepo.findUserByChatId(testUser.chatId())).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(testUser, testLink))
                .isInstanceOf(ScrapperSqlException.class);
    }
}
