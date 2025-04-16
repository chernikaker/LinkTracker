package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;

public interface TagService {

    void addTagsForUserAndLink(User user, Link link, List<Tag> tags);

    void deleteTagForUser(User user, Tag tag);

    void deleteTagForSubscriptionData(User user, Link link, String text);

    List<Tag> getTagsBySubscriptionId(long id);

    List<Tag> getTagsForUser(User user);
}
