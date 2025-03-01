package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.repository.SubscriptionNotExistsException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class InMemorySubscriptionRepository {

    private static final AtomicLong ID = new AtomicLong(0);
    private final Map<Long, Subscription> subscriptions = new HashMap<>();

    public long addSubscription(Subscription subscription) {
        long id = getSubscriptionId(subscription.userId(), subscription.linkId());
        if (id != -1) {
            throw new ScrapperSubscriptionAlreadyExistsException("Subscription to " + subscription.link()
                    + " already exists for user " + subscription.user() + " untrack first");
        }
        long newLinkId = ID.incrementAndGet();
        subscriptions.put(newLinkId, subscription);
        return newLinkId;
    }

    public Subscription removeSubscriptionById(long id) {
        Subscription deleted = subscriptions.remove(id);
        if (deleted == null) {
            throw new SubscriptionNotExistsException("Subscription " + id + " does not exist");
        }
        return deleted;
    }

    public Set<Subscription> deleteUserSubscriptions(User user) {
        Map<Long, Subscription> subscriptions = getUserSubscriptions(user);
        for (Long id : subscriptions.keySet()) {
            removeSubscriptionById(id);
        }
        return new HashSet<>(subscriptions.values());
    }

    public Map<Long, Subscription> getUserSubscriptions(User user) {
        return subscriptions.entrySet().stream()
                .filter(entry -> entry.getValue().user().equals(user))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<Subscription> getLinkSubscriptions(Link link) {
        return subscriptions.values().stream()
                .filter(subscription -> subscription.link().equals(link))
                .collect(Collectors.toList());
    }

    public long getSubscriptionId(long userId, long linkId) {
        for (Map.Entry<Long, Subscription> entry : subscriptions.entrySet()) {
            Subscription sub = entry.getValue();
            if (sub.linkId() == linkId && sub.userId() == userId) {
                return entry.getKey();
            }
        }
        return -1;
    }
}
