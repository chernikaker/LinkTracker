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
            if(existingLink.isPresent()) {
                linkId = existingLink.get().id();
                checkExistingSubscription(u.id(), linkId);
            } else {
                linkId = linkRepo.addLink(new SqlLink(link.url(), link.lastValidation()));
            }
            long subscrId = subscrRepo.addSubscription(new SqlSubscription(u.id(), linkId));
            for (Tag t : tags) {
                Optional<SqlTag> existingTag = tagRepo.getTagByTextAndUserId(u.id(), t.value());
                long tagId = existingTag.map(SqlTag::id)
                    .orElseGet(() -> tagRepo.addTag(new SqlTag(t.value(), u.id())));
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
    public Map<Long, Subscription> getUserSubscriptions(User user){
        try {
            SqlUser u = tryGetUserByChatId(user.chatId());
            Map<Long, Subscription> ans = new HashMap<>();
            for (SqlSubscription s :subscrRepo.getSubscriptionsByUserId(u.id())){
                SqlLink l = linkRepo.getLinkById(s.linkId());
                Link link = new Link(l.url(), LinkType.fromValue(l.url()), l.lastValidation());
                List<Tag> tags = mapSubscriptionTags(s.id());
                List<Filter> filters = mapSubscriptionFilters(s.id());
                ans.put(s.id(), new Subscription(user, link, tags, filters));
            }
            return ans;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting user links ", e);
        }
    }

    @Override
    @Transactional
    public List<Long> getSubscribersChatsByLinkId(long linkId) {
        try {
            return subscrRepo.getChatsByLink(linkId);
        } catch (DataAccessException e){
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
            if(subscrRepo.getSubscriptionsByLink(l.id()).isEmpty()) {
                linkRepo.deleteLinkById(l.id());
            }
            Link resultLink = new Link(link.url(), link.type(), l.lastValidation());
            return Map.entry(s.id(), new Subscription(user, resultLink, tags, filters));
        } catch (ScrapperLinkNotExistsException e){
           throw new ScrapperSubscriptionNotExistsException("Subscription not exists: "+e.getMessage());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while removing subscription with SQL "+e.getMessage(), e);
        }
    }

    private SqlUser tryGetUserByChatId(long chatId){
        return userRepo.findUserByChatId(chatId)
            .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

    private void checkExistingSubscription(long userId, long linkId){
        if(subscrRepo.getSubscriptionByLinkAndUserId(linkId, userId)
            .isPresent()) {
            throw new ScrapperSubscriptionAlreadyExistsException("Subscription " + linkId + " already exists");
        }
    }

    private List<Tag> mapSubscriptionTags(long subscriptionId){
        List<Tag> tags = new ArrayList<>();
        for (SqlTag t: tagRepo.getSubscriptionTags(subscriptionId)) {
            tags.add(new Tag(t.tagText()));
        }
        return tags;
    }

    private List<Filter> mapSubscriptionFilters(long subscriptionId){
        List<Filter> filters = new ArrayList<>();
        for (SqlFilter f: filterRepo.getFiltersBySubscriptionId(subscriptionId)) {
            filters.add(new Filter(f.key(), f.value()));
        }
        return filters;
    }

    private SqlLink tryGetLinkByUrl(String url) {
        return linkRepo.findLinkByUrl(url)
            .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + url + " does not exist"));
    }

    private SqlSubscription tryGetSubscriptionByLinkAndUserId(long linkId, long userId) {
        return subscrRepo.getSubscriptionByLinkAndUserId(linkId, userId)
            .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription for link " +linkId+" by user "+userId + " does not exist"));
    }

}
