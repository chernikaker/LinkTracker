package backend.academy.scrapper.service.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
import backend.academy.scrapper.service.ScrapperService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScrapperServiceTest {

    @Mock
    private GithubClientService githubService;

    @Mock
    private StackoverflowClientService stackoverflowService;

    @Mock
    private InMemoryUserRepository userRepository;

    @Mock
    private InMemoryLinkRepository linkRepository;

    @Mock
    private InMemorySubscriptionRepository subscriptionRepository;

    @InjectMocks
    private ScrapperService scrapperService;

    @Test
    public void getUserLinksTest_UserHaveLinks() {
        Link l = new Link("url", null, null);
        Subscription s = new Subscription(1, null, 10, l, List.of("tag"), List.of("filter"));
        when(subscriptionRepository.getUserSubscriptions(any())).thenReturn(Map.of(1L, s));

        ListLinksResponse response = scrapperService.getUserLinks(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        LinkResponse link = response.links().getFirst();
        assertEquals(1, link.id());
        assertEquals("url", link.url());
        assertEquals(1, link.tags().size());
        assertEquals("tag", link.tags().getFirst());
        assertEquals(1, link.filters().size());
        assertEquals("filter", link.filters().getFirst());
    }

    @Test
    public void getUserLinksTest_UserNotHaveLinks() {
        when(subscriptionRepository.getUserSubscriptions(any())).thenReturn(new HashMap<>());

        ListLinksResponse response = scrapperService.getUserLinks(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
        assertTrue(response.links().isEmpty());
    }

    @Test
    public void addSubscriptionTest_AvailableLink() {
        long userId = 1L;
        when(githubService.isLinkAvailable(any())).thenReturn(true);
        when(subscriptionRepository.addSubscription(any())).thenReturn(2L);
        AddLinkRequest request = new AddLinkRequest("link", List.of(), List.of());
        try (MockedStatic<LinkType> mockedStatic = Mockito.mockStatic(LinkType.class)) {
            mockedStatic.when(() -> LinkType.fromValue(request.link())).thenReturn(LinkType.GITHUB);

            LinkResponse response = assertDoesNotThrow(() -> scrapperService.addSubscription(userId, request));

            assertNotNull(response);
            assertEquals(2L, response.id());
            assertEquals(0, response.tags().size());
            assertEquals(0, response.filters().size());
        }
    }

    @Test
    public void addSubscriptionTest_UnavailableLink() {
        long userId = 1L;
        when(githubService.isLinkAvailable(any())).thenReturn(false);
        AddLinkRequest request = new AddLinkRequest("link", List.of(), List.of());
        try (MockedStatic<LinkType> mockedStatic = Mockito.mockStatic(LinkType.class)) {
            mockedStatic.when(() -> LinkType.fromValue(request.link())).thenReturn(LinkType.GITHUB);

            assertThatThrownBy(() -> scrapperService.addSubscription(userId, request))
                    .isInstanceOf(ScrapperUnavailableLinkException.class)
                    .hasMessageContaining("Link is unavailable");
        }
    }

    @Test
    public void deleteSubscriptionTest_Successful() {
        long userId = 3L;
        Link link = new Link("url", null, null);
        RemoveLinkRequest request = new RemoveLinkRequest("url");
        when(userRepository.containsUser(userId)).thenReturn(true);
        when(linkRepository.getLinkIdByURL(any())).thenReturn(1L);
        when(subscriptionRepository.getSubscriptionId(userId, 1L)).thenReturn(2L);
        when(subscriptionRepository.removeSubscriptionById(2L))
                .thenReturn(new Subscription(userId, null, 1L, link, List.of("tag"), List.of("filter")));

        LinkResponse response = assertDoesNotThrow(() -> scrapperService.deleteSubscription(userId, request));

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("url", response.url());
        assertEquals(1, response.tags().size());
        assertEquals("tag", response.tags().getFirst());
        assertEquals(1, response.filters().size());
        assertEquals("filter", response.filters().getFirst());
    }

    @Test
    public void deleteSubscriptionTest_PresentLinkNoSubscriptionException() {
        long userId = 3L;
        RemoveLinkRequest request = new RemoveLinkRequest("url");
        when(linkRepository.getLinkIdByURL(any())).thenReturn(1L);
        when(subscriptionRepository.getSubscriptionId(userId, 1L)).thenReturn(-1L);
        when(userRepository.containsUser(userId)).thenReturn(true);

        assertThatThrownBy(() -> scrapperService.deleteSubscription(userId, request))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionTest_NoLinkException() {
        long userId = 3L;
        RemoveLinkRequest request = new RemoveLinkRequest("url");
        when(linkRepository.getLinkIdByURL(any())).thenReturn(-1L);
        when(userRepository.containsUser(userId)).thenReturn(true);

        assertThatThrownBy(() -> scrapperService.deleteSubscription(userId, request))
                .isInstanceOf(ScrapperLinkNotExistsException.class);
    }

    @Test
    public void deleteSubscriptionTest_NoUserException() {
        long userId = 3L;
        RemoveLinkRequest request = new RemoveLinkRequest("url");

        assertThatThrownBy(() -> scrapperService.deleteSubscription(userId, request))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }
}
