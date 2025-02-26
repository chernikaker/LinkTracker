package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.custom.ScrapperSubscriptionAlreadyExistsException;
import backend.academy.scrapper.exception.custom.SubscriptionNotExistsException;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

    public List<Subscription> getUserSubscriptions(User user) {
        List<Subscription> response = new ArrayList<>();
        for (Map.Entry<Long, Subscription> entry: subscriptions.entrySet()){
            if (entry.getValue().user().equals(user)) {
                response.add(entry.getValue());
            }
        }
        return response;
    }

    public List<Subscription> getLinkSubscriptions(Link link) {
        List<Subscription> response = new ArrayList<>();
        for (Map.Entry<Long, Subscription> entry: subscriptions.entrySet()){
            if (entry.getValue().link().equals(link)) {
                response.add(entry.getValue());
            }
        }
        return response;
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
