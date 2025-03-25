package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import java.util.Map;


public interface DatabaseService {

    void addUser(User user);

    long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters);


    long removeSubscriptionOnLink(User user, Link link);

    void deleteTagForUser(User user, Tag tag);

    void deleteTagForSubscription(Subscription subscription, String text);


    List<Tag> getSubscriptionTagsById(long id);

    List<Filter> getSubscriptionFiltersById(long id);

    Map<Long, Subscription> getUserSubscriptions(User user);

    void removeSubscriptionAdditionalInfoById(long subscriptionId);
}
