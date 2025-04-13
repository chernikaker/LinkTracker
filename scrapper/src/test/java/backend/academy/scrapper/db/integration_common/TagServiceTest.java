package backend.academy.scrapper.db.integration_common;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
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
public abstract class TagServiceTest {

    public static final User USER = new User(1L);
    public static final Tag TAG = new Tag("tag");
    public static final Link LINK =
            new Link("https://github.com/1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

    @Autowired
    private TagService tagService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void deleteTagForUser_TagWithNoSubs() {
        fillDataOnlyTag(USER, TAG);

        assertDoesNotThrow(() -> tagService.deleteTagForUser(USER, TAG));
        assertEquals(0, findAllAmount("tag"));
    }

    @Test
    public void deleteTagForUser_TagWithSubs() {
        Long tagId = fillDataReturnTagId(USER, TAG);

        assertDoesNotThrow(() -> tagService.deleteTagForUser(USER, TAG));
        assertEquals(0, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription"));
        assertEquals(1, findAllAmount("link"));
        assertEquals(0, findSubWithTagAmount(tagId));
    }

    @Test
    public void deleteTagForUser_UserNotExists() {
        assertThatThrownBy(() -> tagService.deleteTagForUser(USER, TAG))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteTagForUser_TagNotExists() {
        fillDataUserOnly(USER);

        assertThatThrownBy(() -> tagService.deleteTagForUser(USER, TAG))
                .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForUser_TagNotExistsForThatUser() {
        fillDataUserOnly(USER);
        fillDataOnlyTag(new User(2L), TAG);

        assertThatThrownBy(() -> tagService.deleteTagForUser(USER, TAG))
                .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscriptionData_Success() {
        fillDataReturnTagId(USER, TAG);

        assertDoesNotThrow(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG.value()));
        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription"));
        assertEquals(0, findAllAmount("subscription_tag"));
    }

    @Test
    public void deleteTagForSubscriptionData_UserNotExists() {
        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG.value()))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscriptionData_TagNotExists() {
        var ids = fillDataUserOnly(USER);
        addSubscription(ids, addLink());

        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG.value()))
                .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscriptionData_SubscriptionNotExists() {
        fillDataOnlyTag(USER, TAG);
        addLink();

        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG.value()))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void getTagsBySubscriptionId_Success() {
        var ids = fillDataOnlyTag(USER, TAG);
        Long linkId = addLink();
        Long subId = addSubscriptionWithTag(ids.getKey(), linkId, ids.getValue());

        List<Tag> tags = assertDoesNotThrow(() -> tagService.getTagsBySubscriptionId(subId));
        assertEquals(1, tags.size());
        assertEquals(TAG.value(), tags.getFirst().value());
    }

    private Long fillDataUserOnly(User user) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id", Long.class, user.chatId());
    }

    private Long addLink() {
        return jdbcTemplate.queryForObject(
                "INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
                Long.class,
                LINK.url(),
                LINK.lastValidation());
    }

    private Map.Entry<Long, Long> fillDataOnlyTag(User user, Tag tag) {
        Long userId = fillDataUserOnly(user);
        Long tagId = jdbcTemplate.queryForObject(
                "INSERT INTO tag (tag_text, user_id) VALUES(?,?) RETURNING id", Long.class, tag.value(), userId);
        return Map.entry(userId, tagId);
    }

    private Long addSubscription(long userId, long linkId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription (user_id, link_id) VALUES(?,?) RETURNING id", Long.class, userId, linkId);
    }

    private Long addSubscriptionWithTag(long userId, long linkId, long tagId) {
        Long subscriptionId = addSubscription(userId, linkId);
        jdbcTemplate.update("INSERT INTO subscription_tag(subscription_id, tag_id) VALUES(?,?)", subscriptionId, tagId);
        return subscriptionId;
    }

    private Long fillDataReturnTagId(User user, Tag tag) {
        var ids = fillDataOnlyTag(user, tag);
        Long linkId = addLink();
        addSubscriptionWithTag(ids.getKey(), linkId, ids.getValue());
        return ids.getValue();
    }

    private Long findAllAmount(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    private Long findSubWithTagAmount(Long tagId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscription_tag WHERE tag_id = ?", Long.class, tagId);
    }
}
