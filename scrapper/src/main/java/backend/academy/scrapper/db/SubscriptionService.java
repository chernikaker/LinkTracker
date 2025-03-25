package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import java.util.Map;


public interface SubscriptionService {

    long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters);

    long removeSubscriptionOnLink(User user, Link link);

    Map<Long, Subscription> getUserSubscriptions(User user);

}
