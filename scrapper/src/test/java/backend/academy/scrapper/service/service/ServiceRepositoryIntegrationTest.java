package backend.academy.scrapper.service.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
import backend.academy.scrapper.service.ScrapperService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            ScrapperService.class,
            InMemoryLinkRepository.class,
            InMemorySubscriptionRepository.class,
            InMemoryUserRepository.class
        })
public class ServiceRepositoryIntegrationTest {

    @Autowired
    private ScrapperService scrapperService;

    @Autowired
    private InMemoryLinkRepository linkRepository;

    @Autowired
    private InMemorySubscriptionRepository subscriptionRepository;

    @Autowired
    private InMemoryUserRepository userRepository;

    @MockitoBean
    private GithubClientService githubClientService;

    @MockitoBean
    private StackoverflowClientService stackoverflowClientService;

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
    public void testRegisterUser() {
        scrapperService.registerUser(1L);

        User registeredUser = assertDoesNotThrow(() -> userRepository.getUserById(1L));
        assertEquals(1L, registeredUser.chatId());
    }

    @Test
    public void testDeleteUser_AllInfoDeletedWithLink() {
        linkRepository.addLink(link);
        userRepository.registerUser(user);
        subscriptionRepository.addSubscription(subscription);

        scrapperService.deleteUser(1L);

        assertFalse(userRepository.containsUser(1L));
        assertTrue(subscriptionRepository.getUserSubscriptions(user).isEmpty());
        assertFalse(linkRepository.containsLink(1L));
    }

    @Test
    public void testDeleteUser_AllInfoDeletedLinkRemains() {
        linkRepository.addLink(link);
        Subscription otherSubscription = new Subscription(2L, new User(2L), 1L, link, List.of(), List.of());
        userRepository.registerUser(user);
        subscriptionRepository.addSubscription(subscription);
        subscriptionRepository.addSubscription(otherSubscription);

        scrapperService.deleteUser(1L);

        assertFalse(userRepository.containsUser(1L));
        assertTrue(subscriptionRepository.getUserSubscriptions(user).isEmpty());
        assertTrue(linkRepository.containsLink(1L));
    }

    @Test
    public void testGetUserLinks_success() {
        linkRepository.addLink(link);
        userRepository.registerUser(user);
        subscriptionRepository.addSubscription(subscription);

        ListLinksResponse response = assertDoesNotThrow(() -> scrapperService.getUserLinks(1L));

        assertEquals(1, response.size());
        assertEquals("https://github.com/mock", response.links().getFirst().url());
    }

    @Test
    public void testGetUserLinks_notRegisteredUser() {
        linkRepository.addLink(link);
        subscriptionRepository.addSubscription(subscription);

        assertThatThrownBy(() -> scrapperService.getUserLinks(1L)).isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void testGetUserLinks_noLinks() {
        linkRepository.addLink(link);
        userRepository.registerUser(user);

        ListLinksResponse response = assertDoesNotThrow(() -> scrapperService.getUserLinks(1L));

        assertEquals(0, response.size());
    }

    @Test
    public void testAddSubscription_NewLink() {
        userRepository.registerUser(user);
        when(githubClientService.isLinkAvailable(any())).thenReturn(true);
        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        LinkResponse response = assertDoesNotThrow(() -> scrapperService.addSubscription(1L, request));

        assertEquals(1L, response.id());
        assertEquals("https://github.com/mock", response.url());
        assertTrue(response.tags().isEmpty());
        assertTrue(response.filters().isEmpty());

        assertNotEquals(-1, linkRepository.getLinkIdByURL("https://github.com/mock"));
        assertTrue(subscriptionRepository.containsSubscription(response.id()));
    }

    @Test
    public void testAddSubscription_ExistingLink() {
        linkRepository.addLink(link);
        userRepository.registerUser(user);
        when(githubClientService.isLinkAvailable(any())).thenReturn(true);
        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        LinkResponse response = assertDoesNotThrow(() -> scrapperService.addSubscription(1L, request));

        assertEquals(1L, response.id());
        assertEquals("https://github.com/mock", response.url());
        assertTrue(response.tags().isEmpty());
        assertTrue(response.filters().isEmpty());

        assertNotEquals(-1, linkRepository.getLinkIdByURL("https://github.com/mock"));
        assertTrue(subscriptionRepository.containsSubscription(response.id()));
    }

    @Test
    public void testAddSubscription_LinkUnavailable() {
        userRepository.registerUser(user);
        when(githubClientService.isLinkAvailable(any(Link.class))).thenReturn(false);
        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        assertThatThrownBy(() -> scrapperService.addSubscription(1L, request))
                .isInstanceOf(ScrapperUnavailableLinkException.class);
    }

    @Test
    public void testAddSubscription_UnregisteredUser() {
        when(githubClientService.isLinkAvailable(any(Link.class))).thenReturn(true);
        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        assertThatThrownBy(() -> scrapperService.addSubscription(1L, request))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void testAddSubscription_SubscriptionAlreadyExists() {
        linkRepository.addLink(link);
        userRepository.registerUser(user);
        subscriptionRepository.addSubscription(new Subscription(1L, user, 1L, link, List.of(), List.of()));
        when(githubClientService.isLinkAvailable(any())).thenReturn(true);

        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        assertThatThrownBy(() -> scrapperService.addSubscription(1L, request))
                .isInstanceOf(ScrapperSubscriptionAlreadyExistsException.class);
    }

    @Test
    public void testDeleteSubscription_LinkRemoves() {
        userRepository.registerUser(user);
        long linkId = linkRepository.addLink(link);
        subscriptionRepository.addSubscription(subscription);

        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/mock");
        LinkResponse response = assertDoesNotThrow(() -> scrapperService.deleteSubscription(1L, request));

        assertEquals(1L, response.id());
        assertEquals("https://github.com/mock", response.url());
        assertFalse(subscriptionRepository.containsSubscription(response.id()));
        assertFalse(linkRepository.containsLink(linkId));
    }

    @Test
    public void testDeleteSubscription_LinkNotRemoves() {
        userRepository.registerUser(user);
        long linkId = linkRepository.addLink(link);
        Subscription other = new Subscription(2L, new User(2L), 1L, link, List.of(), List.of());
        subscriptionRepository.addSubscription(subscription);
        subscriptionRepository.addSubscription(other);
        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/mock");

        LinkResponse response = assertDoesNotThrow(() -> scrapperService.deleteSubscription(1L, request));

        assertEquals(1L, response.id());
        assertEquals("https://github.com/mock", response.url());
        assertFalse(subscriptionRepository.containsSubscription(response.id()));
        assertTrue(linkRepository.containsLink(linkId));
    }

    @Test
    public void testDeleteSubscription_LinkNotExists() {
        userRepository.registerUser(user);

        RemoveLinkRequest request = new RemoveLinkRequest("url");

        assertThatThrownBy(() -> scrapperService.deleteSubscription(1L, request))
                .isInstanceOf(ScrapperLinkNotExistsException.class);
    }

    @Test
    public void testDeleteSubscription_UserNotExists() {
        RemoveLinkRequest request = new RemoveLinkRequest("url");

        assertThatThrownBy(() -> scrapperService.deleteSubscription(1L, request))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void testDeleteSubscription_SubscriptionNotExists() {
        userRepository.registerUser(user);
        linkRepository.addLink(link);

        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/mock");

        assertThatThrownBy(() -> scrapperService.deleteSubscription(1L, request))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }
}
