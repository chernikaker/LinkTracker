package backend.academy.scrapper.db.sql.service;

import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.sql.entity.SqlFilter;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.entity.SqlTag;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.FilterSqlRepository;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.TagSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionAlreadyExistsException;
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

/** SQL реализация сервиса работы с подписками */
@AllArgsConstructor
public class SqlSubscriptionService implements SubscriptionService {

    private final UserSqlRepository userRepo;
    private final LinkSqlRepository linkRepo;
    private final SubscriptionSqlRepository subscrRepo;
    private final TagSqlRepository tagRepo;
    private final FilterSqlRepository filterRepo;

    @Override
    @Transactional
    public long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters) {
        try {
            SqlUser u = tryGetUserByChatId(user.chatId());
            Optional<SqlLink> existingLink = linkRepo.findLinkByUrl(link.url());
            long linkId;
            // если ссылка уже есть, добавляем подписку к ней, иначе создаем новую ссылку
            if (existingLink.isPresent()) {
                SqlLink sqlLink = existingLink.orElseThrow();
                linkId = sqlLink.id();
                // проверка на существование подписки
                checkExistingSubscription(u.id(), linkId);
            } else {
                linkId = linkRepo.addLink(new SqlLink(link.url(), link.lastValidation()));
            }
            long subscrId = subscrRepo.addSubscription(new SqlSubscription(u.id(), linkId));
            for (Tag t : tags) {
                Optional<SqlTag> existingTag = tagRepo.getTagByTextAndUserId(u.id(), t.value());
                long tagId = existingTag.map(SqlTag::id).orElseGet(() -> tagRepo.addTag(new SqlTag(t.value(), u.id())));
                tagRepo.addTagToSubscription(tagId, subscrId);
            }
            for (Filter f : filters) {
                SqlFilter fSql = new SqlFilter(f.key(), f.value(), subscrId, u.id());
                filterRepo.addFilterToSubscription(fSql);
            }
            return subscrId;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding link with SQL", e);
        }
    }

    @Override
    @Transactional
    public Map<Long, Subscription> getUserSubscriptions(User user) {
        try {
            SqlUser u = tryGetUserByChatId(user.chatId());
            List<SqlSubscription> subscriptions = subscrRepo.getSubscriptionsByUserId(u.id());
            return processSqlSubsListForUser(subscriptions, user);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting user links ", e);
        }
    }

    @Override
    @Transactional
    public List<Subscription> getSubscriptionsByLinkId(long linkId) {
        try {
            SqlLink l = linkRepo.getLinkById(linkId);
            List<SqlSubscription> subscriptions = subscrRepo.getSubscriptionsByLink(l.id());
            return processSqlSubsListForSqlLink(subscriptions, l);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting user links ", e);
        }
    }

    @Override
    @Transactional
    public Map.Entry<Long, Subscription> deleteSubscriptionByUserAndLink(User user, Link link) {
        try {
            SqlUser u = tryGetUserByChatId(user.chatId());
            SqlLink l = tryGetLinkByUrl(link.url());
            SqlSubscription s = tryGetSubscriptionByLinkAndUserId(l.id(), u.id());
            List<Tag> tags = mapSubscriptionTags(s.id());
            List<Filter> filters = mapSubscriptionFilters(s.id());
            subscrRepo.deleteSubscriptionById(s.id());
            // если у ссылки нет подписок, удаляем ее
            if (subscrRepo.getSubscriptionsByLink(l.id()).isEmpty()) {
                linkRepo.deleteLinkById(l.id());
            }
            Link resultLink = new Link(link.url(), link.type(), l.lastValidation());
            return Map.entry(s.id(), new Subscription(user, resultLink, tags, filters));
        } catch (ScrapperLinkNotExistsException e) {
            throw new ScrapperSubscriptionNotExistsException("Subscription not exists: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while removing subscription with SQL " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Map<Long, Subscription> deleteSubscriptionsByUserAndTag(User user, Tag tag) {
        try {
            SqlUser u = tryGetUserByChatId(user.chatId());
            SqlTag t = tryGetTagByUserAndText(tag, u);
            List<SqlSubscription> subscriptions = subscrRepo.getSubscriptionsByTagId(t.id());
            Map<Long, Subscription> ans = processSqlSubsListForUser(subscriptions, user);
            subscrRepo.deleteSubscriptionsByTagId(t.id());
            for (SqlSubscription s : subscriptions) {
                // если у ссылки нет подписок, удаляем ее
                if (subscrRepo.getSubscriptionsByLink(s.linkId()).isEmpty()) {
                    linkRepo.deleteLinkById(s.linkId());
                }
            }
            return ans;

        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while getting user links ", e);
        }
    }

    @Override
    public Map<Long, Subscription> getSubscriptionsByUserAndTag(User user, Tag tag) {
        try {
            SqlUser u = tryGetUserByChatId(user.chatId());
            SqlTag t = tryGetTagByUserAndText(tag, u);
            List<SqlSubscription> subscriptions = subscrRepo.getSubscriptionsByTagId(t.id());
            return processSqlSubsListForUser(subscriptions, user);

        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while getting user links ", e);
        }
    }

    private SqlUser tryGetUserByChatId(long chatId) {
        return userRepo.findUserByChatId(chatId)
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

    private void checkExistingSubscription(long userId, long linkId) {
        if (subscrRepo.getSubscriptionByLinkAndUserId(linkId, userId).isPresent()) {
            throw new ScrapperSubscriptionAlreadyExistsException("Subscription " + linkId + " already exists");
        }
    }

    private List<Tag> mapSubscriptionTags(long subscriptionId) {
        List<Tag> tags = new ArrayList<>();
        for (SqlTag t : tagRepo.getSubscriptionTags(subscriptionId)) {
            tags.add(new Tag(t.tagText()));
        }
        return tags;
    }

    private List<Filter> mapSubscriptionFilters(long subscriptionId) {
        List<Filter> filters = new ArrayList<>();
        for (SqlFilter f : filterRepo.getFiltersBySubscriptionId(subscriptionId)) {
            filters.add(new Filter(f.key(), f.value()));
        }
        return filters;
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

    private Map<Long, Subscription> processSqlSubsListForUser(List<SqlSubscription> subs, User user) {
        Map<Long, Subscription> ans = new HashMap<>();
        for (SqlSubscription s : subs) {
            SqlLink l = linkRepo.getLinkById(s.linkId());
            Link link = new Link(l.url(), LinkType.fromValue(l.url()), l.lastValidation());
            List<Tag> tags = mapSubscriptionTags(s.id());
            List<Filter> filters = mapSubscriptionFilters(s.id());
            ans.put(s.id(), new Subscription(user, link, tags, filters));
        }
        return ans;
    }

    private List<Subscription> processSqlSubsListForSqlLink(List<SqlSubscription> subs, SqlLink l) {
        List<Subscription> ans = new ArrayList<>();
        Link link = new Link(l.url(), LinkType.fromValue(l.url()), l.lastValidation());
        for (SqlSubscription s : subs) {
            SqlUser u = userRepo.findUserById(s.userId());
            User user = new User(u.chatId());
            List<Tag> tags = mapSubscriptionTags(s.id());
            List<Filter> filters = mapSubscriptionFilters(s.id());
            ans.add(new Subscription(user, link, tags, filters));
        }
        return ans;
    }

    private SqlTag tryGetTagByUserAndText(Tag tag, SqlUser u) {
        return tagRepo.getTagByTextAndUserId(u.id(), tag.value())
                .orElseThrow(() -> new ScrapperTagNotExistsException("Tag not exists: " + tag.value()));
    }
}
