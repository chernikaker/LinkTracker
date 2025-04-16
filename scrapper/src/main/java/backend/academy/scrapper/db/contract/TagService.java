package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import java.util.Map;

public interface TagService {

    Map<Long, Tag> addTagsForUserAndLink(User user, Link link, List<Tag> tags);

    Map.Entry<Long, Tag> deleteTagForUser(User user, Tag tag);

    Map.Entry<Long, Tag> deleteTagForSubscriptionData(User user, Link link, String text);

    List<Tag> getTagsBySubscriptionId(long id);

    Map<Long, Tag> getTagsForUser(User user);
}
