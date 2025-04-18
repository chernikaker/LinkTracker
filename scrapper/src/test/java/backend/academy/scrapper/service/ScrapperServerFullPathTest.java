package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.db.config.SqlConfig;
import java.time.LocalDateTime;
import java.time.ZoneId;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import({TestClientConfig.class, SqlConfig.class})
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class ScrapperServerFullPathTest {

    private static final String ADD_SUB_REQUEST =
            """
        {
            "link": "https://github.com/mock",
            "tags": [],
            "filters": []
        }
        """;

    private static final String DELETE_SUB_REQUEST =
            """
        {
            "link": "https://github.com/mock"
        }
        """;

    private static final String ADD_TAG_REQUEST =
        """
        {
            "link": "https://github.com/mock",
            "tags": [
                "tag"
            ]
        }
        """;

    private static final String REMOVE_SUB_TAG_REQUEST= """
        {
        "link": "https://github.com/mock",
        "tag": "tag"
        }
        """;

    private static final String REMOVE_TAG_REQUEST =
        """
        {
        "tag" : "tag"
        }
      """;
    public static final String URL = "https://github.com/mock";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @SneakyThrows
    public void registerChatTest_validId() {
        long chatId = 1L;

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        assertTrue(containsUserWithChat(chatId));
    }

    @Test
    @SneakyThrows
    public void registerChatTest_invalidId() {
        long invalidChatId = -1L;

        mockMvc.perform(post("/tg-chat/{id}", invalidChatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));

        assertFalse(containsUserWithChat(invalidChatId));
    }

    @Test
    @SneakyThrows
    public void registerChatTest_userAlreadyExists() {
        long chatId = 1L;

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isBadRequest());

        assertTrue(containsUserWithChat(chatId));
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_validId() {
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        assertFalse(containsUserWithChat(chatId));
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_userNotExists() {
        long chatId = 1L;

        mockMvc.perform(delete("/tg-chat/{id}", chatId))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    @Test
    @SneakyThrows
    public void getLinksTest_successful() {
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId).accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(0));
    }

    @Test
    @SneakyThrows
    public void getLinksTest_invalidId() {
        long invalidChatId = -1L;

        mockMvc.perform(get("/links").header("Tg-Chat-Id", invalidChatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void getLinksTest_serviceException_userNotFound() {
        long chatId = 1L;

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperUserNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscriptionTest_Success() {
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content(ADD_SUB_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(URL))
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.filters").isEmpty());

        assertTrue(containsLinkWithUrl(URL));
        assertEquals(1, getSubAmountByChatIdAndLinkUrl(chatId, URL));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscriptionTest_subscriptionAlreadyExist() {
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content(ADD_SUB_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperSubscriptionAlreadyExistsException"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_Success() {
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content(DELETE_SUB_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(URL));

        assertEquals(0, getSubAmountByChatIdAndLinkUrl(1L, URL));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_linkNotExistsException() {
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content(DELETE_SUB_REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_subscriptionNotExistsException() {
        long chatId = 1L;
        addUser(chatId);
        addLink(URL);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json")
                        .content(DELETE_SUB_REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_Success(){
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);

        mockMvc.perform(post("/links/tags")
            .header("Tg-Chat-Id", chatId)
            .contentType("application/json")
            .content(ADD_TAG_REQUEST))
            .andExpect(status().isOk());

        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription_tag"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_TagsExistDoesNotFail(){
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);
        addTagOnSubscriptionWithMockMvc(chatId);

        mockMvc.perform(post("/links/tags")
            .header("Tg-Chat-Id", chatId)
            .contentType("application/json")
            .content(ADD_TAG_REQUEST))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tags.size").value(1L));
        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription_tag"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_SubscriptionNotExistException(){
        long chatId = 1L;
        addLink(URL);
        addUser(chatId);

        mockMvc.perform(post("/links/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(ADD_TAG_REQUEST))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_Success(){
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);
        addTagOnSubscriptionWithMockMvc(chatId);

        mockMvc.perform(delete("/links/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isOk());

        assertEquals(0, findAllAmount("subscription_tag"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_SubscriptionNotExistsException(){
        long chatId = 1L;
        addUser(chatId);
        addLink(URL);

        mockMvc.perform(delete("/links/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperSubscriptionNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_TagNotExistsForSubscriptionException(){
        long chatId = 1L;
        long id = addUser(chatId);
        addLink(URL);
        addSubscriptionWithMockMvc(chatId);
        addTagToUser(id);

        mockMvc.perform(delete("/links/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_Success(){
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);
        addTagOnSubscriptionWithMockMvc(chatId);

        mockMvc.perform(delete("/tags/tag/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isOk());

        assertEquals(0, getSubAmountByChatIdAndLinkUrl(1L, URL));
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_SuccessNoSubs(){
        long chatId = 1L;
        long id = addUser(chatId);
        addTagToUser(id);

        mockMvc.perform(delete("/tags/tag/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.links.size").value(0L));
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_TagNotExistException(){
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);

        mockMvc.perform(delete("/tags/tag/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperTagNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_Success(){
        long chatId = 1L;
        long id = addUser(chatId);
        addTagToUser(id);

        mockMvc.perform(delete("/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isOk());

        assertEquals(0 ,findAllAmount("tag"));
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_TagNotFoundException(){
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(delete("/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_UserNotFoundException(){
        long chatId = 1L;

        mockMvc.perform(delete("/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void getUserTags_Success(){
        long chatId = 1L;
        long id = addUser(chatId);
        addTagToUser(id);

        mockMvc.perform(get("/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(1L));
    }

    @Test
    @SneakyThrows
    public void getUserTags_SuccessNoTags() {
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(get("/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(0L));
    }

    @Test
    @SneakyThrows
    public void getUserTags_UserNotFoundException() {
        long chatId = 1L;

        mockMvc.perform(get("/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_Success(){
        long chatId = 1L;
        addUser(chatId);
        addSubscriptionWithMockMvc(chatId);
        addTagOnSubscriptionWithMockMvc(chatId);

        mockMvc.perform(get("/tags/tag/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.links.size").value(1L));
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_SuccessNoSubs(){
        long chatId = 1L;
        long id = addUser(chatId);
        addTagToUser(id);

        mockMvc.perform(get("/tags/tag/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.links.size").value(0L));
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_TagNotFoundException(){
        long chatId = 1L;
        addUser(chatId);

        mockMvc.perform(get("/tags/tag/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperTagNotExistsException"));
    }

    private void addTagOnSubscriptionWithMockMvc(long chatId) throws Exception {
        mockMvc.perform(post("/links/tags")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(ADD_TAG_REQUEST));
    }

    private void addSubscriptionWithMockMvc(long chatId) throws Exception {
        mockMvc.perform(post("/links")
            .header("Tg-Chat-Id", chatId)
            .contentType("application/json")
            .content(ADD_SUB_REQUEST));
    }


    private Boolean containsUserWithChat(long chatId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM tg_user WHERE chat_id=?)", Boolean.class, chatId);
    }

    private Long getSubAmountByChatIdAndLinkUrl(long chatId, String url) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscription s " + "JOIN tg_user u ON u.id = s.user_id "
                        + "JOIN link l ON l.id = s.link_id WHERE l.url = ? AND u.chat_id = ?",
                Long.class,
                url,
                chatId);
    }

    private void addTagToUser(long chatId){
        jdbcTemplate.update("INSERT INTO tag (tag_text, user_id) VALUES (?, ?)", "tag", chatId);
    }
    private Boolean containsLinkWithUrl(String url) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM link WHERE url = ?)", Boolean.class, url);
    }

    private Long addUser(long chatId){
        return jdbcTemplate.queryForObject("INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id", Long.class, chatId);
    }

    private void addLink(String url) {
        jdbcTemplate.update(
                "INSERT INTO link (url, last_validation) VALUES (?, ?)",
                url,
                LocalDateTime.now(ZoneId.systemDefault()));
    }

    private Long findAllAmount(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }
}
