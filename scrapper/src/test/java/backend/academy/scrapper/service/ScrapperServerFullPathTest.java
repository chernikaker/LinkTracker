package backend.academy.scrapper.service;

import backend.academy.scrapper.db.config.SqlConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        mockMvc.perform(post("/tg-chat/{id}", chatId))
            .andExpect(status().isBadRequest());

        assertTrue(containsUserWithChat(chatId));
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_validId() {
        long chatId = 1L;
        mockMvc.perform(post("/tg-chat/{id}", chatId));

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
        mockMvc.perform(post("/tg-chat/{id}", chatId));

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
        mockMvc.perform(post("/tg-chat/{id}", chatId));

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(ADD_SUB_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://github.com/mock"))
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.filters").isEmpty());

        assertTrue(containsLinkWithUrl("https://github.com/mock"));
        assertEquals(1, getSubAmountByChatIdAndLinkUrl(chatId, "https://github.com/mock"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscriptionTest_subscriptionAlreadyExist() {
        long chatId = 1L;
        mockMvc.perform(post("/tg-chat/{id}", chatId));

        mockMvc.perform(
            post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(ADD_SUB_REQUEST));

        mockMvc.perform(
                        post("/links")
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
        mockMvc.perform(post("/tg-chat/{id}", chatId));
        mockMvc.perform(
            post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType("application/json")
                .content(ADD_SUB_REQUEST));

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(DELETE_SUB_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://github.com/mock"));

        assertEquals(0, getSubAmountByChatIdAndLinkUrl(1L, "https://github.com/mock"));
    }


    @Test
    @SneakyThrows
    public void deleteLinkSubscription_linkNotExistsException() {
        long chatId = 1L;
        mockMvc.perform(post("/tg-chat/{id}", chatId));

        mockMvc.perform(
                        delete("/links")
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
        mockMvc.perform(post("/tg-chat/{id}", chatId));
        addLink("https://github.com/mock");

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(DELETE_SUB_REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    private Boolean containsUserWithChat(long chatId){
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM tg_user WHERE chat_id=?)", Boolean.class, chatId);
    }

    private Long getSubAmountByChatIdAndLinkUrl(long chatId, String url){
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscription s " +
            "JOIN tg_user u ON u.id = s.user_id " +
            "JOIN link l ON l.id = s.link_id WHERE l.url = ? AND u.chat_id = ?", Long.class, url, chatId);
    }

    private Boolean containsLinkWithUrl(String url){
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM link WHERE url = ?)", Boolean.class, url);
    }

    private void addLink(String url){
        jdbcTemplate.update("INSERT INTO link (url, last_validation) VALUES (?, ?)", url, LocalDateTime.now(ZoneId.systemDefault()));
    }
}
