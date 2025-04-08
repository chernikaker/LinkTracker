package backend.academy.scrapper.db.integration;

import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Transactional
public abstract class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void addDifferentUsers_Success() {
        User user = new User(1L);
        User user2 = new User(2L);

        assertDoesNotThrow(() -> userService.addUser(user));
        assertDoesNotThrow(() -> userService.addUser(user2));
        Long userId1 = findUserByChatId(1L);
        Long userId2 = findUserByChatId(2L);

        assertNotNull(userId1);
        assertNotNull(userId2);
        assertNotEquals(userId1, userId2);
    }

    @Test
    public void addUser_AlreadyExistsException() {
        User user = new User(1L);

        assertDoesNotThrow(() -> userService.addUser(user));
        assertThatThrownBy(() -> userService.addUser(user))
            .isInstanceOf(ScrapperUserAlreadyExistsException.class);
        assertEquals(1, findAllAmount("tg_user"));
    }

    @Test
    public void deleteUserNoSubscriptions_Success() {
        User user = new User(1L);
        userService.addUser(user);

        assertDoesNotThrow(() -> userService.deleteUser(user));
        Long userId = findUserByChatId(1L);
        assertNull(userId);
        assertEquals(0, findAllAmount("tg_user"));
    }

    @Test
    public void deleteUserWithAdditionalDataAndUniqueLink_Success() {
        User user = new User(1L);
        fillData(user);

        assertDoesNotThrow(() -> userService.deleteUser(user));
        Long userId = findUserByChatId(1L);
        assertNull(userId);
        assertEquals(0, findAllAmount("tg_user"));
        assertEquals(0, findAllAmount("subscription"));
        assertEquals(0, findAllAmount("tag"));
        assertEquals(0, findAllAmount("filter"));
        assertEquals(0, findAllAmount("link"));
    }

    @Test
    public void deleteUserWithAdditionalDataAndNonUniqueLink_Success() {
        User user = new User(1L);
        User user2 = new User(2L);
        fillData(user, user2);

        assertDoesNotThrow(() -> userService.deleteUser(user));
        Long userId = findUserByChatId(1L);
        assertNull(userId);
        assertEquals(1, findAllAmount("tg_user"));
        assertEquals(1, findAllAmount("subscription"));
        assertEquals(0, findAllAmount("tag"));
        assertEquals(0, findAllAmount("filter"));
        assertEquals(1, findAllAmount("link"));
    }

    @Test
    public void deleteUser_NotExistsException() {
        User user = new User(1L);

        assertThatThrownBy(() -> userService.deleteUser(user))
            .isInstanceOf(ScrapperUserNotExistsException.class);
    }


    private Long findUserByChatId(Long chatId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id FROM tg_user WHERE chat_id = ?",
                Long.class,
                chatId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Long findAllAmount(String table) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM "+table,
            Long.class
        );
    }

    private Long fillData(User user) {
        Long userId = jdbcTemplate.queryForObject(
            "INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id",
            Long.class, user.chatId());
        Long linkId = jdbcTemplate.queryForObject("INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
        Long.class, "https://github.com/1", LocalDateTime.now(ZoneId.systemDefault()));
        Long subscriptionId = jdbcTemplate.queryForObject("INSERT INTO subscription (user_id, link_id) VALUES(?,?) RETURNING id",
            Long.class, userId, linkId);
        Long tagId = jdbcTemplate.queryForObject("INSERT INTO tag (tag_text, user_id) VALUES(?,?) RETURNING id",
            Long.class, "tag", userId);
        jdbcTemplate.update("INSERT INTO filter(key, value, subscription_id, user_id) VALUES(?,?,?,?)",
            "key", "value", subscriptionId, userId);
        jdbcTemplate.update("INSERT INTO subscription_tag(subscription_id, tag_id) VALUES(?,?)",
            subscriptionId, tagId);
        return linkId;
    }

    private void fillData(User user1, User user2){
        Long linkId = fillData(user1);
        Long userId = jdbcTemplate.queryForObject(
            "INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id",
            Long.class, user2.chatId());
        jdbcTemplate.update("INSERT INTO subscription (user_id, link_id) VALUES(?,?)",
            userId, linkId);
    }
}
