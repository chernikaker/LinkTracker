package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.custom.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.custom.repository.SubscriptionNotExistsException;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemorySubscriptionRepository {

    private static final AtomicLong ID = new AtomicLong(0);
    private final Map<Long, Subscription> subscriptions = new HashMap<>();

    public long addSubscription(Subscription subscription) {
        long id = getSubscriptionId(subscription);
        if (id != -1) {
            throw new ScrapperSubscriptionAlreadyExistsException(
                "Subscription to " + subscription.link() + " already exists for user " + subscription.user()
                + " untrack first");
        }
        long newLinkId = ID.incrementAndGet();
        subscriptions.put(newLinkId, subscription);
        return newLinkId;
    }

    public void removeSubscriptionById(long id) {
        if (subscriptions.remove(id) != null) {
            throw new SubscriptionNotExistsException("Link " + id + " does not exist");
        }
    }

    public void deleteUserSubscriptions(User user) {
        Map<Long, Subscription> subscriptions = getUserSubscriptions(user);
        for(Long id : subscriptions.keySet()) {
            removeSubscriptionById(id);
        }
    }

    public Map<Long, Subscription> getUserSubscriptions(User user) {
        return subscriptions
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().user().equals(user))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Long, Subscription> getLinkSubscriptions(Link link) {
        return subscriptions
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().user().equals(link))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private long getSubscriptionId(Subscription subscription) {
        for (Map.Entry<Long, Subscription> entry : subscriptions.entrySet()) {
            Subscription sub = entry.getValue();
            if (sub.link().equals(subscription.link()) && sub.user().equals(subscription.user())) {
                return entry.getKey();
            }
        }
        return -1;
    }
}
