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
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class SqlTagService implements TagService {

    private final UserSqlRepository userRepo;
    private final LinkSqlRepository linkRepo;
    private final SubscriptionSqlRepository subscrRepo;
    private final TagSqlRepository tagRepo;

    @Override
    @Transactional
    public void addTagsForUserAndLink(User user, Link link, List<Tag> tags) {
        try {
            SqlUser existingUser = tryGetUserByChatId(user.chatId());
            SqlLink existingLink = tryGetLinkByUrl(link.url());
            SqlSubscription sub = tryGetSubscriptionByLinkAndUserId(existingLink.id(), existingUser.id());
            for(Tag tag : tags) {
                Optional<SqlTag> t = tagRepo.getTagByValue(tag.value());
                Long tagId = t.map(SqlTag::id).orElseGet(() -> tagRepo.addTag(new SqlTag(tag.value(), existingUser.id())));
                tagRepo.addTagToSubscription(tagId, sub.id());
            }
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding tag for user " + user.chatId(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForUser(User user, Tag tag) {
        try {
            SqlUser existingUser = tryGetUserByChatId(user.chatId());
            SqlTag existingTag = tryGetTagByTextAndUserId(tag.value(), existingUser.id());
            tagRepo.removeTagById(existingTag.id());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while deleting tag for user " + user.chatId(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForSubscriptionData(User u, Link l, String text) {
        try {
            SqlUser user = tryGetUserByChatId(u.chatId());
            SqlLink link = tryGetLinkByUrl(l.url());
            SqlSubscription existingSub = tryGetSubscriptionByLinkAndUserId(link.id(), user.id());
            SqlTag tag = tryGetTagByTextAndUserId(text, user.id());
            tagRepo.removeTagFromSubscription(tag.id(), existingSub.id());
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
    public List<Tag> getTagsForUser(User user) {
        try {
            SqlUser existingUser = tryGetUserByChatId(user.chatId());
            List<SqlTag> tags = tagRepo.getSubscriptionTags(existingUser.id());
            List<Tag> response = new ArrayList<>();
            for (SqlTag tag : tags) {
                response.add(new Tag(tag.tagText()));
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
}
