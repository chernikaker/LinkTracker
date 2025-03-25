package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;


public interface DatabaseService {

    void addUser(User user);

    void addSubscriptionOnLink(User user, Link link);


    void removeSubscriptionOnLink(User user, Link link);

    void deleteTagForUser(User user, Tag tag);

    void deleteTagForSubscription(Subscription subscription, String text);

    List<Link> getLinksToCheck(int batchSize, long offset, long duration);

    void updateLinkValidationNow(Link link);


    List<Subscription> getUserLinks(User user);
}
