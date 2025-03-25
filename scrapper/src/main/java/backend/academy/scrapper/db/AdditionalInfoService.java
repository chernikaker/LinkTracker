package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;

public interface AdditionalInfoService {

    void deleteTagForUser(User user, Tag tag);

    void deleteTagForSubscription(Subscription subscription, String text);

    List<Tag> getSubscriptionTagsById(long id);

    List<Filter> getSubscriptionFiltersById(long id);

}
