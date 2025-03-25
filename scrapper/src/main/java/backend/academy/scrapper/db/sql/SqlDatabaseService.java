package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.DatabaseService;
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
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
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
public class SqlDatabaseService implements DatabaseService {

    private final UserSqlRepository userRepo;
    private final LinkSqlRepository linkRepo;
    private final SubscriptionSqlRepository subscrRepo;
    private final TagSqlRepository tagRepo;
    private final FilterSqlRepository filterRepo;

    @Override
    @Transactional
    public void addUser(User user) {
        try {
            Optional<SqlUser> existingUser = userRepo.findUserByChatId(user.chatId());
            if (existingUser.isPresent()) {
                throw new ScrapperUserAlreadyExistsException("User " + user.chatId() + " already exists");
            }
            SqlUser userForDb = new SqlUser(user.chatId());
            userRepo.addUser(userForDb);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding user with SQL", e);
        }
    }

    @Override
    @Transactional
    public long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters) {
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            Optional<SqlLink> existingLink = linkRepo.findLinkByUrl(link.url());
            long linkId = existingLink.map(SqlLink::id).orElseGet(() -> linkRepo.addLink(new SqlLink(link.url(), link.lastValidation())));

            long subscrId = subscrRepo.addSubscription(new SqlSubscription(u.id(), linkId));
            for(Tag tag : tags) {
                // TODO: repeating tags
                SqlTag t = new SqlTag(tag.value(), u.id());
                long tagId = tagRepo.addTag(t);
                tagRepo.addTagToSubscription(tagId, subscrId);
            }
            for(Filter filter : filters) {
                SqlFilter f = new SqlFilter(filter.key(), filter.value(), subscrId, u.id());
                filterRepo.addFilterToSubscription(f);
            }
            return subscrId;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding link with SQL", e);
        }
    }

    @Override
    @Transactional
    public long removeSubscriptionOnLink(User user, Link link) {
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            SqlLink l = linkRepo.findLinkByUrl(link.url())
                .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + link.url() + " does not exist"));
            SqlSubscription existingSub = subscrRepo.getSubscriptionByLinkAndUserId(l.id(), u.id())
                .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription " + link.url() + " does not exist"));
            subscrRepo.deleteSubscriptionById(existingSub.id());
            List<SqlSubscription> subs = subscrRepo.getSubscriptionsByLink(l.id());
            if(subs.isEmpty()){
                linkRepo.deleteLinkById(l.id());
            }
            return existingSub.id();
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding link with SQL", e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForUser(User user, Tag tag) {
        try {
        SqlUser existingUser = userRepo.findUserByChatId(user.chatId())
            .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));

        SqlTag existingTag = tagRepo.getTagByTextAndUserId(existingUser.id(), tag.value())
            .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + tag.value() + " does not exist"));

            List<SqlSubscription> userSubs = subscrRepo.getSubscriptionsByUserId(existingUser.id());
            userSubs.forEach(sub -> tagRepo.removeTagFromSubscription(existingTag.id(), sub.id()));
            tagRepo.removeTagById(existingTag.id());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while deleting tag for user " + user.chatId(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForSubscription(Subscription subscription, String text) {
        try {
        SqlLink link = linkRepo.findLinkByUrl(subscription.link().url())
            .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + subscription.link().url() + " does not exist"));
        SqlUser user = userRepo.findUserByChatId(subscription.user().chatId())
            .orElseThrow(() -> new ScrapperUserNotExistsException("User " + subscription.user().chatId() + " does not exist"));
        SqlSubscription existingSub = subscrRepo.getSubscriptionByLinkAndUserId(link.id(), user.id())
            .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription " + subscription.link().url() + " does not exist"));
        SqlTag tag = tagRepo.getTagByTextAndUserId(user.id(), text)
            .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + text + " does not exist"));

            tagRepo.removeTagFromSubscription(tag.id(), existingSub.id());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while deleting tag from link " + subscription.link().url(), e);
        }
    }

    @Override
    public List<Tag> getSubscriptionTagsById(long id) {
        try{
            List<SqlTag> tags = tagRepo.getSubscriptionTags(id);
            List<Tag> response = new ArrayList<>();
            for(SqlTag tag : tags){
                response.add(new Tag(tag.tagText()));
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting subscription tags", e);
        }
    }

    @Override
    public List<Filter> getSubscriptionFiltersById(long id) {
        try {
            List<SqlFilter> filters = filterRepo.getFiltersBySubscriptionId(id);
            List<Filter> response = new ArrayList<>();
            for(SqlFilter filter : filters){
                response.add(new Filter(filter.key(), filter.value()));
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting filters", e);
        }
    }


    @Override
    @Transactional
    public Map<Long, Subscription> getUserSubscriptions(User user){
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            Map<Long, Subscription> ans = new HashMap<>();
            for (SqlSubscription s :subscrRepo.getSubscriptionsByUserId(u.id())){
                SqlLink l = linkRepo.getLinkById(s.linkId());
                Link link = new Link(l.url(), LinkType.fromValue(l.url()), l.lastValidation());
                List<Tag> tags = new ArrayList<>();
                for (SqlTag t: tagRepo.getSubscriptionTags(s.id())) {
                    tags.add(new Tag(t.tagText()));
                }
                List<Filter> filters = new ArrayList<>();
                for (SqlFilter f: filterRepo.getFiltersBySubscriptionId(s.id())) {
                    filters.add(new Filter(f.key(), f.value()));
                }
                ans.put(s.id(), new Subscription(user, link, tags, filters));
            }
            return ans;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting user links", e);
        }
    }

    @Transactional
    @Override
    public void removeSubscriptionAdditionalInfoById(long subscriptionId) {
        try {
            tagRepo.removeAllTagsFromSubscriptionById(subscriptionId);
            filterRepo.removeFiltersBySubscriptionId(subscriptionId);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while removing subscription additional info", e);
        }
    }
}
