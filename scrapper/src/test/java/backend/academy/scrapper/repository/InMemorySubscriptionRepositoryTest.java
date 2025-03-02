package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.repository.SubscriptionNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemorySubscriptionRepositoryTest {

    private InMemorySubscriptionRepository repository;
    private User user;
    private Link link;
    private Subscription subscription;

    @BeforeEach
    public void setUp() {
        repository = new InMemorySubscriptionRepository();
        user = new User(1L);
        link = new Link("url", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        subscription = new Subscription(1L, user, 1L, link, List.of(), List.of());
    }

    @Test
    public void addNewSubscription() {
        long subscriptionId = repository.addSubscription(subscription);

        assertEquals(1L, subscriptionId);
        assertTrue(repository.containsSubscription(subscriptionId));
    }

    @Test
    public void addSubscription_AlreadyExists() {
        repository.addSubscription(subscription);

        assertThatThrownBy(() -> repository.addSubscription(subscription))
            .isInstanceOf(ScrapperSubscriptionAlreadyExistsException.class)
            .hasMessageContaining("already exists for user");
    }

    @Test
    public void removeSubscriptionById() {
        long subscriptionId = repository.addSubscription(subscription);

        Subscription s = assertDoesNotThrow(() -> repository.removeSubscriptionById(subscriptionId));
        assertEquals(s, subscription);
        assertFalse(repository.containsSubscription(subscriptionId));
    }

    @Test
    public void removeSubscriptionById_NotExists() {
        assertThatThrownBy(() -> repository.removeSubscriptionById(1L))
            .isInstanceOf(SubscriptionNotExistsException.class)
            .hasMessageContaining("does not exist");
    }

    @Test
    public void deleteUserSubscriptions() {
        repository.addSubscription(subscription);
        Subscription anotherSubscription = new Subscription(2L, user, 3L, null, List.of(), List.of());
        repository.addSubscription(anotherSubscription);

        Set<Subscription> removedSubscriptions = repository.deleteUserSubscriptions(user);

        assertThat(removedSubscriptions).containsExactlyInAnyOrder(subscription, anotherSubscription);
        assertTrue(repository.getUserSubscriptions(user).isEmpty());
    }

    @Test
    public void getUserSubscriptions() {
        repository.addSubscription(subscription);
        User anotherUser = new User(2L);
        Subscription anotherSubscription = new Subscription(2L, anotherUser, 3L, null, List.of(), List.of());
        repository.addSubscription(anotherSubscription);

        Map<Long, Subscription> userSubscriptions = repository.getUserSubscriptions(user);

        assertEquals(1, userSubscriptions.size());
        assertThat(userSubscriptions.values()).containsExactly(subscription);
    }

    @Test
    public void getUserSubscriptions_UserHasNoSubscriptions() {
        Map<Long, Subscription> userSubscriptions = repository.getUserSubscriptions(user);

        assertEquals(0, userSubscriptions.size());
    }

    @Test
    public void getLinkSubscriptions() {
        repository.addSubscription(subscription);
        User anotherUser = new User(2L);
        Subscription anotherSubscription = new Subscription(2L, anotherUser, 1L, link, List.of(), List.of());
        repository.addSubscription(anotherSubscription);

        List<Subscription> linkSubscriptions = repository.getLinkSubscriptions(link);

        assertThat(linkSubscriptions).containsExactlyInAnyOrder(subscription, anotherSubscription);
    }

    @Test
    public void getLinkSubscriptions_NoSubscriptions() {
        List<Subscription> linkSubscriptions = repository.getLinkSubscriptions(link);

        assertEquals(0, linkSubscriptions.size());
    }

    @Test
    public void getSubscriptionId_shouldReturnCorrectId() {
        long subscriptionId = repository.addSubscription(subscription);
        long foundId = repository.getSubscriptionId(subscription.userId(), subscription.linkId());

        assertEquals(subscriptionId, foundId);
    }

    @Test
    public void getSubscriptionId_shouldReturnMinusOneIfNotExists() {
        long subscriptionId = repository.getSubscriptionId(999L, 999L);

        assertEquals(-1L, subscriptionId);
    }
}
