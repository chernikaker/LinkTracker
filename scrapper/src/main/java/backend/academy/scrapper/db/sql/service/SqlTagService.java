package backend.academy.scrapper.db.sql.service;

import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.entity.SqlTag;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.TagSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class SqlTagService implements TagService {

    private final UserSqlRepository userRepo;
    private final LinkSqlRepository linkRepo;
    private final SubscriptionSqlRepository subscrRepo;
    private final TagSqlRepository tagRepo;

    @Override
    @Transactional
    public Map<Long, Tag> addTagsForUserAndLink(User user, Link link, List<Tag> tags) {
        try {
            SqlUser existingUser = tryGetUserByChatId(user.chatId());
            SqlLink existingLink = tryGetLinkByUrl(link.url());
            SqlSubscription sub = tryGetSubscriptionByLinkAndUserId(existingLink.id(), existingUser.id());
            Map<Long, Tag> addedTags = new HashMap<>();
            for (Tag tag : tags) {
                Optional<SqlTag> t = tagRepo.getTagByValue(tag.value());
                Long tagId =
                        t.map(SqlTag::id).orElseGet(() -> tagRepo.addTag(new SqlTag(tag.value(), existingUser.id())));
                tagRepo.addTagToSubscription(tagId, sub.id());
                addedTags.put(tagId, tag);
            }
            return addedTags;
        } catch (ScrapperLinkNotExistsException e) {
            throw new ScrapperSubscriptionNotExistsException("Subscription not exists on link " + link.url());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding tag for user " + user.chatId(), e);
        }
    }

    @Override
    @Transactional
    public Map.Entry<Long, Tag> deleteTagForUser(User user, Tag tag) {
        try {
            SqlUser existingUser = tryGetUserByChatId(user.chatId());
            SqlTag existingTag = tryGetTagByTextAndUserId(tag.value(), existingUser.id());
            tagRepo.removeTagById(existingTag.id());
            return Map.entry(existingTag.id(), tag);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while deleting tag for user " + user.chatId(), e);
        }
    }

    @Override
    @Transactional
    public Map.Entry<Long, Tag> deleteTagForSubscriptionData(User u, Link l, String text) {
        try {
            SqlUser user = tryGetUserByChatId(u.chatId());
            SqlLink link = tryGetLinkByUrl(l.url());
            SqlSubscription existingSub = tryGetSubscriptionByLinkAndUserId(link.id(), user.id());
            SqlTag tag = tryGetTagByTextAndSubscriptionId(text, existingSub.id());
            tagRepo.removeTagFromSubscription(tag.id(), existingSub.id());
            return Map.entry(tag.id(), new Tag(text));
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while deleting tag from link " + l.url(), e);
        }
    }

    @Override
    public List<Tag> getTagsBySubscriptionId(long id) {
        try {
            List<SqlTag> tags = tagRepo.getSubscriptionTags(id);
            List<Tag> response = new ArrayList<>();
            for (SqlTag tag : tags) {
                response.add(new Tag(tag.tagText()));
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting subscription tags", e);
        }
    }

    @Override
    public Map<Long, Tag> getTagsForUser(User user) {
        try {
            SqlUser existingUser = tryGetUserByChatId(user.chatId());
            List<SqlTag> tags = tagRepo.getUserTagsById(existingUser.id());
            Map<Long, Tag> response = new HashMap<>();
            for (SqlTag tag : tags) {
                response.put(tag.id(), new Tag(tag.tagText()));
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting user tags", e);
        }
    }

    private SqlUser tryGetUserByChatId(long chatId) {
        return userRepo.findUserByChatId(chatId)
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

    private SqlLink tryGetLinkByUrl(String url) {
        return linkRepo.findLinkByUrl(url)
                .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + url + " does not exist"));
    }

    private SqlSubscription tryGetSubscriptionByLinkAndUserId(long linkId, long userId) {
        return subscrRepo
                .getSubscriptionByLinkAndUserId(linkId, userId)
                .orElseThrow(() -> new ScrapperSubscriptionNotExistsException(
                        "Subscription for link " + linkId + " by user " + userId + " does not exist"));
    }

    private SqlTag tryGetTagByTextAndUserId(String text, long userId) {
        return tagRepo.getTagByTextAndUserId(userId, text)
                .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + text + " does not exist"));
    }

    private SqlTag tryGetTagByTextAndSubscriptionId(String text, long subId) {
        return tagRepo.getTagByTextAndSubscriptionId(subId, text)
                .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + text + " does not exist"));
    }
}
