package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestClientConfig.class)
@AutoConfigureMockMvc
public class ScrapperServerFullPathTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryLinkRepository linkRepository;

    @Autowired
    private InMemorySubscriptionRepository subscriptionRepository;

    @Autowired
    private InMemoryUserRepository userRepository;

    private User user;
    private Link link;
    private Subscription subscription;

    @BeforeEach
    public void setUp() {
        linkRepository.clear();
        subscriptionRepository.clear();
        userRepository.clear();
        user = new User(1L);
        link = new Link("https://github.com/mock", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        subscription = new Subscription(1L, user, 1L, link, List.of(), List.of());
    }

    @Test
    @SneakyThrows
    public void registerChatTest_validId() {
        long chatId = 1L;

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        assertTrue(userRepository.containsUser(1L));
    }

    @Test
    @SneakyThrows
    public void registerChatTest_invalidId() {
        long invalidChatId = -1L;

        mockMvc.perform(post("/tg-chat/{id}", invalidChatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));

        assertFalse(userRepository.containsUser(-1L));
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_validId() {
        long chatId = userRepository.registerUser(user);

        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        assertFalse(userRepository.containsUser(1L));
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
        userRepository.registerUser(user);

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
    public void addLinkSubscriptionTest_newLinkSuccess() {
        long chatId = 1L;
        userRepository.registerUser(user);
        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://github.com/mock",
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value("https://github.com/mock"))
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.filters").isEmpty());

        assertTrue(linkRepository.containsLink(1L));
        assertTrue(subscriptionRepository.getUserSubscriptions(user).containsKey(1L));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscriptionTest_existingLinkSuccess() {
        long chatId = 1L;
        userRepository.registerUser(user);
        linkRepository.addLink(link);
        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://github.com/mock",
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value("https://github.com/mock"))
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.filters").isEmpty());

        assertEquals(1, linkRepository.size());
        assertTrue(subscriptionRepository.getUserSubscriptions(user).containsKey(1L));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscriptionTest_subscriptionAlreadyExist() {
        long chatId = 1L;
        userRepository.registerUser(user);
        linkRepository.addLink(link);
        subscriptionRepository.addSubscription(subscription);
        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://github.com/mock",
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperSubscriptionAlreadyExistsException"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_successUniqueLink() {
        long chatId = 1L;
        userRepository.registerUser(user);
        linkRepository.addLink(link);
        subscriptionRepository.addSubscription(subscription);

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://github.com/mock"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value(link.url()));

        assertTrue(subscriptionRepository.getUserSubscriptions(user).isEmpty());
        assertEquals(0, linkRepository.size());
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_successLinkHasOtherSubscription() {
        long chatId = 1L;
        userRepository.registerUser(user);
        linkRepository.addLink(link);
        Subscription s = new Subscription(2L, new User(2L), 1L, link, List.of(), List.of());
        subscriptionRepository.addSubscription(subscription);
        subscriptionRepository.addSubscription(s);

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://github.com/mock"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value(link.url()));

        assertTrue(subscriptionRepository.getUserSubscriptions(user).isEmpty());
        assertEquals(1, linkRepository.size());
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_linkNotExistsException() {
        long chatId = 1L;
        userRepository.registerUser(user);
        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "url"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_subscriptionNotExistsException() {
        long chatId = 1L;
        userRepository.registerUser(user);
        linkRepository.addLink(link);
        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "url"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }
}
