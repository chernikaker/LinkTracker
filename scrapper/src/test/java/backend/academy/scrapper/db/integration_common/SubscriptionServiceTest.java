package backend.academy.scrapper.db.integration_common;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class SubscriptionServiceTest {

    private static final User USER = new User(1L);
    public static final Link LINK =
            new Link("https://github.com/1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
    public static final Tag TAG = new Tag("tag");
    public static final Filter FILTER = new Filter("key", "value");

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void addSubscription_SuccessNewLink() {
        addUser(USER);

        assertDoesNotThrow(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)));
        assertEquals(1, findAllAmount("subscription"));
        assertEquals(1, findAllAmount("link"));
        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("filter"));
        assertEquals(1, findAllAmount("subscription_tag"));
    }

    @Test
    public void addSubscription_SuccessExistingLink() {
        addUser(USER);
        addLink(LINK);

        assertDoesNotThrow(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of(FILTER)));
        assertEquals(1, findAllAmount("link"));
    }

    @Test
    public void addSubscription_SuccessNoTagsAndFilters() {
        addUser(USER);
        addLink(LINK);

        assertDoesNotThrow(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(), List.of()));
        assertEquals(0, findAllAmount("tag"));
        assertEquals(0, findAllAmount("filter"));
        assertEquals(0, findAllAmount("subscription_tag"));
    }

    @Test
    public void addSubscription_SubscriptionWithExistingTag() {
        Long id = addUser(USER);
        addTagForUser(id, TAG);

        assertDoesNotThrow(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(TAG), List.of()));
        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription_tag"));
    }

    @Test
    public void addSubscription_UserNotFound() {
        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(), List.of()))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void addSubscription_SubscriptionAlreadyExists() {
        addSubscription(addUser(USER), addLink(LINK));

        assertThatThrownBy(() -> subscriptionService.addSubscriptionOnLink(USER, LINK, List.of(), List.of()))
                .isInstanceOf(ScrapperSubscriptionAlreadyExistsException.class);
    }

    @Test
    public void getUserSubscriptions_SubscriptionsWithAdditionalInfo() {
        fillAllSubscriptionData(true);

        Map<Long, Subscription> subs = assertDoesNotThrow(() -> subscriptionService.getUserSubscriptions(USER));
        assertEquals(1, subs.size());
        Subscription subscription = subs.values().iterator().next();
        assertEquals(USER.chatId(), subscription.user().chatId());
        assertEquals(LINK.url(), subscription.link().url());
        assertEquals(1, subscription.tags().size());
        assertEquals(TAG.value(), subscription.tags().getFirst().value());
        assertEquals(1, subscription.filters().size());
        assertEquals(FILTER.value(), subscription.filters().getFirst().value());
        assertEquals(FILTER.key(), subscription.filters().getFirst().key());
    }

    @Test
    public void getUserSubscriptions_NoAdditionalInfo() {
        fillAllSubscriptionData(false);

        Map<Long, Subscription> subs = assertDoesNotThrow(() -> subscriptionService.getUserSubscriptions(USER));
        assertEquals(1, subs.size());
        Subscription subscription = subs.values().iterator().next();
        assertTrue(subscription.tags().isEmpty());
        assertTrue(subscription.filters().isEmpty());
    }

    @Test
    public void getUserSubscriptions_NoSubscriptions() {
        addUser(USER);

        Map<Long, Subscription> subs = assertDoesNotThrow(() -> subscriptionService.getUserSubscriptions(USER));
        assertTrue(subs.isEmpty());
    }

    @Test
    public void getUserSubscriptions_UserNotFound() {
        assertThatThrownBy(() -> subscriptionService.getUserSubscriptions(USER))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void getSubscribersChatsByLinkId_ManySubscribers() {
        User other = new User(2L);
        Long lId = addLink(LINK);
        addSubscription(addUser(other), lId);
        addSubscription(addUser(USER), lId);

        List<Long> chats = assertDoesNotThrow(() -> subscriptionService.getSubscribersChatsByLinkId(lId));
        assertEquals(2, chats.size());
        assertThat(chats, containsInAnyOrder(other.chatId(), USER.chatId()));
    }

    @Test
    public void getSubscribersChatsByLinkId_NoSubscribers() {
        Long lId = addLink(LINK);

        List<Long> chats = assertDoesNotThrow(() -> subscriptionService.getSubscribersChatsByLinkId(lId));
        assertTrue(chats.isEmpty());
    }

    @Test
    public void deleteSubscriptionByUserAndLink_AdditionalInfoAndUniqueLinkSubscription() {
        fillAllSubscriptionData(true);

        var deleted = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK));
        assertEquals(0, findAllAmount("subscription"));
        assertEquals(0, findAllAmount("link"));
        assertEquals(1, findAllAmount("user"));
        assertEquals(1, findAllAmount("tag"));
        assertEquals(0, findAllAmount("filter"));
        assertEquals(0, findAllAmount("subscription_tag"));
        assertEquals(LINK.url(), deleted.getValue().link().url());
        assertEquals(USER.chatId(), deleted.getValue().user().chatId());
        assertEquals(1, deleted.getValue().tags().size());
        assertEquals(TAG.value(), deleted.getValue().tags().getFirst().value());
        assertEquals(1, deleted.getValue().filters().size());
        assertEquals(FILTER.value(), deleted.getValue().filters().getFirst().value());
        assertEquals(FILTER.key(), deleted.getValue().filters().getFirst().key());
    }

    @Test
    public void deleteSubscriptionByUserAndLink_NoAdditionalInfo() {
        fillAllSubscriptionData(false);

        var deleted = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK));
        assertTrue(deleted.getValue().tags().isEmpty());
        assertTrue(deleted.getValue().filters().isEmpty());
    }

    @Test
    public void deleteSubscriptionByUserAndLink_RepeatingLinkSubscription() {
        fillAllSubscriptionData(false);
        User other = new User(2L);
        addUser(other);
        subscriptionService.addSubscriptionOnLink(other, LINK, List.of(), List.of());

        var deleted = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK));
        assertEquals(1, findAllAmount("link"));
    }

    @Test
    public void deleteSubscriptionByUserAndLink_UserNotExists() {
        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionByUserAndLink_SubscriptionNotExists() {
        addUser(USER);
        addLink(LINK);

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionByUserAndLink(USER, LINK))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void getSubscriptionsByUserAndTag_Success(){
        fillAllSubscriptionData(true);

        Map<Long, Subscription> subs = assertDoesNotThrow(() -> subscriptionService.getSubscriptionsByUserAndTag(USER, TAG));

        assertEquals(1, subs.size());
        Subscription subscription = subs.values().iterator().next();
        assertEquals(1, subscription.tags().size());
        assertEquals(TAG.value(), subscription.tags().getFirst().value());
    }

    @Test
    public void getSubscriptionsByUserAndTag_SuccessNoSubs(){
        Long uId = addUser(USER);
        addTagForUser(uId, TAG);

        Map<Long, Subscription> subs = assertDoesNotThrow(() -> subscriptionService.getSubscriptionsByUserAndTag(USER, TAG));

        assertTrue(subs.isEmpty());
    }

    @Test
    public void getSubscriptionsByUserAndTag_TagNotExists(){
        addUser(USER);

        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByUserAndTag(USER, TAG))
            .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void getSubscriptionsByUserAndTag_UserNotExists(){
        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByUserAndTag(USER, TAG))
            .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionsByUserAndTag_SuccessDeletingWithLink(){
        fillAllSubscriptionData(true);

        Map<Long, Subscription> subs = assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionsByUserAndTag(USER, TAG));

        assertEquals(1, subs.size());
        Subscription subscription = subs.values().iterator().next();
        assertEquals(1, subscription.tags().size());
        assertEquals(TAG.value(), subscription.tags().getFirst().value());
        assertEquals(0, findAllAmount("link"));
        assertEquals(0, findAllAmount("subscription_tag"));
        assertEquals(0, findAllAmount("subscription"));
        assertEquals(1, findAllAmount("tag"));
    }

    @Test
    public void deleteSubscriptionsByUserAndTag_SuccessDeletingWithoutLink(){
        Long linkId = fillAllSubscriptionData(true);
        User other = new User(2L);
        addSubscription(addUser(other), linkId);

        assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionsByUserAndTag(USER, TAG));

        assertEquals(1, findAllAmount("link"));
        assertEquals(0, findAllAmount("subscription_tag"));
        assertEquals(1, findAllAmount("subscription"));
        assertEquals(1, findAllAmount("tag"));
    }

    @Test
    public void deleteSubscriptionsByUserAndTag_NoSubsDoesNothing(){
        addTagForUser(addUser(USER), TAG);

        assertDoesNotThrow(() -> subscriptionService.deleteSubscriptionsByUserAndTag(USER, TAG));

        assertEquals(1, findAllAmount("tag"));
    }

    @Test
    public void deleteSubscriptionsByUserAndTag_TagNotExist(){
        addUser(USER);

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionsByUserAndTag(USER, TAG))
            .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionsByUserAndTag_UserNotExist(){
        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionsByUserAndTag(USER, TAG))
            .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    private Long addUser(User user) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id", Long.class, user.chatId());
    }

    private Long addLink(Link l) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
                Long.class,
                l.url(),
                l.lastValidation());
    }

    private Long addTagForUser(Long userId, Tag tag) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO tag (tag_text, user_id) VALUES (?, ?) RETURNING id", Long.class, tag.value(), userId);
    }

    private void addFilterForUserAndSub(Long userId, Long subscriptionId, Filter filter) {
        jdbcTemplate.update(
                "INSERT INTO filter (key, value, subscription_id, user_id) VALUES (?, ?, ?, ?)",
                filter.key(),
                filter.value(),
                subscriptionId,
                userId);
    }

    private Long fillAllSubscriptionData(boolean addInfo) {
        Long userId = addUser(USER);
        Long linkId = addLink(LINK);
        Long subId = addSubscription(userId, linkId);
        if (addInfo) {
            addFilterForUserAndSub(userId, subId, FILTER);
            Long tagId = addTagForUser(userId, TAG);
            jdbcTemplate.update("INSERT INTO subscription_tag (subscription_id, tag_id) VALUES (?, ?)", subId, tagId);
        }
        return linkId;
    }

    private Long addSubscription(Long userId, Long linkId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription (user_id, link_id) VALUES (?, ?) RETURNING id", Long.class, userId, linkId);
    }

    private Long findAllAmount(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }
}
