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

/** Репозиторий для хранения информации о подписках */
@Component
public class InMemorySubscriptionRepository {

    // уникальный счетчик ID
    private final AtomicLong ID = new AtomicLong(0);
    private final Map<Long, Subscription> subscriptions = new HashMap<>();

    public long addSubscription(Subscription subscription) {
        long id = getSubscriptionId(subscription.userId(), subscription.linkId());
        // если подписка уже есть, выкидываем исключение
        if (id != -1) {
            throw new ScrapperSubscriptionAlreadyExistsException(
                    "Subscription to %s already exists for user %s untrack first"
                            .formatted(subscription.link(), subscription.user()));
        }
        long newLinkId = ID.incrementAndGet();
        subscriptions.put(newLinkId, subscription);
        return newLinkId;
    }

    public Subscription removeSubscriptionById(long ID) {
        Subscription deleted = subscriptions.remove(ID);
        if (deleted == null) {
            throw new SubscriptionNotExistsException("Subscription " + ID + " does not exist");
        }
        return deleted;
    }

    public boolean containsSubscription(long ID) {
        return subscriptions.containsKey(ID);
    }

    /**
     * Удаление подписок пользователя
     *
     * @param user пользователь
     * @return множество удаленных подписок
     */
    public Set<Subscription> deleteUserSubscriptions(User user) {
        Map<Long, Subscription> subscriptions = getUserSubscriptions(user);
        for (Long id : subscriptions.keySet()) {
            removeSubscriptionById(id);
        }
        return new HashSet<>(subscriptions.values());
    }

    /**
     * Получение подписок пользователя
     *
     * @param user пользователь
     * @return множество подписок и их ID
     */
    public Map<Long, Subscription> getUserSubscriptions(User user) {
        return subscriptions.entrySet().stream()
                .filter(entry -> entry.getValue().user().equals(user))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Получение подписок на определенную ссылку
     *
     * @param link ссылка
     * @return множество подписок
     */
    public List<Subscription> getLinkSubscriptions(Link link) {
        return subscriptions.values().stream()
                .filter(subscription -> subscription.link().equals(link))
                .collect(Collectors.toList());
    }

    /**
     * Получение id ссылки по пользователю и ссылке
     *
     * @param userId id пользователя
     * @param linkId id ссылки
     * @return множество подписок
     */
    public long getSubscriptionId(long userId, long linkId) {
        for (Map.Entry<Long, Subscription> entry : subscriptions.entrySet()) {
            Subscription sub = entry.getValue();
            if (sub.linkId() == linkId && sub.userId() == userId) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void clear() {
        subscriptions.clear();
        ID.set(0);
    }
}
