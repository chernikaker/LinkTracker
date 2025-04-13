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
import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import backend.academy.scrapper.service.ScrapperService;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private static final Link LINK = new Link("url", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
    private static final User USER = new User(1L);
    private static final Tag TAG = new Tag("tag");
    private static final Filter FILTER = new Filter("key", "value");

    @Mock
    private GithubClientService githubService;

    @Mock
    private StackoverflowClientService stackoverflowService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private TagService tagService;

    @Mock
    private FilterService filterService;

    @InjectMocks
    private ScrapperService scrapperService;

    @Test
    public void getUserLinksTest_UserHaveLinks() {
        Subscription s = new Subscription(USER, LINK, List.of(TAG), List.of(FILTER));
        when(subscriptionService.getUserSubscriptions(any())).thenReturn(Map.of(1L, s));

        ListLinksResponse response = scrapperService.getUserLinks(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        LinkResponse link = response.links().getFirst();
        assertEquals(1, link.id());
        assertEquals("url", link.url());
        assertEquals(1, link.tags().size());
        assertEquals("tag", link.tags().getFirst());
        assertEquals(1, link.filters().size());
        assertEquals("key:value", link.filters().getFirst());
    }

    @Test
    public void getUserLinksTest_UserNotHaveLinks() {
        when(subscriptionService.getUserSubscriptions(any())).thenReturn(Map.of());

        ListLinksResponse response = scrapperService.getUserLinks(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
        assertTrue(response.links().isEmpty());
    }

    @Test
    public void addSubscriptionTest_AvailableLink() {
        long userId = 1L;
        when(githubService.isLinkAvailable(any())).thenReturn(true);
        when(subscriptionService.addSubscriptionOnLink(any(), any(), any(), any())).thenReturn(1L);
        AddLinkRequest request = new AddLinkRequest("link", List.of("tag"), List.of("key:value"));
        try (MockedStatic<LinkType> mockedStatic = Mockito.mockStatic(LinkType.class)) {
            mockedStatic.when(() -> LinkType.fromValue(request.link())).thenReturn(LinkType.GITHUB);

            LinkResponse response = assertDoesNotThrow(() -> scrapperService.addSubscription(userId, request));

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(1, response.tags().size());
            assertEquals(1, response.filters().size());
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
                    .isInstanceOf(ScrapperUnavailableLinkException.class);
        }
    }

    @Test
    public void deleteSubscriptionTest_Successful() {
        Subscription s = new Subscription(USER, LINK, List.of(TAG), List.of(FILTER));
        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/1");
        when(subscriptionService.deleteSubscriptionByUserAndLink(any(), any())).thenReturn(Map.entry(1L, s));


        LinkResponse response = assertDoesNotThrow(() -> scrapperService.deleteSubscription(1L, request));

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("https://github.com/1", response.url());
        assertEquals(1, response.tags().size());
        assertEquals("tag", response.tags().getFirst());
        assertEquals(1, response.filters().size());
        assertEquals("key:value", response.filters().getFirst());
    }
}
