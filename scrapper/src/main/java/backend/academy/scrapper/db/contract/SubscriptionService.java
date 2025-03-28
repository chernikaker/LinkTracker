package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import java.util.Map;


public interface SubscriptionService {

    long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters);

    Map<Long, Subscription> getUserSubscriptions(User user);

    List<Long> getLinkSubscribersChatsById(long linkId);

    Map.Entry<Long,Subscription> deleteSubscriptionByUserAndLink(User user, Link link);
}
