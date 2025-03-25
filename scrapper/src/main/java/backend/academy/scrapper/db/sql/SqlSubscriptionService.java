package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.SubscriptionService;
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
            e.printStackTrace();
            throw new ScrapperSqlException("Error while getting user links ", e);
        }
    }

    @Override
    @Transactional
    public Map.Entry<Long, Subscription> deleteSubscriptionByUserAndLink(User user, Link link) {
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            SqlLink l = linkRepo.findLinkByUrl(link.url())
                .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + link.url() + " does not exist"));
            SqlSubscription s = subscrRepo.getSubscriptionByLinkAndUserId(l.id(), u.id())
                .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription " + link.url() + " does not exist"));
            List<Tag> tags = new ArrayList<>();
            for (SqlTag t: tagRepo.getSubscriptionTags(s.id())) {
                tags.add(new Tag(t.tagText()));
            }
            List<Filter> filters = new ArrayList<>();
            for (SqlFilter f: filterRepo.getFiltersBySubscriptionId(s.id())) {
                filters.add(new Filter(f.key(), f.value()));
            }
            subscrRepo.deleteSubscriptionById(s.id());
            return Map.entry(s.id(), new Subscription(user, link, tags, filters));
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new ScrapperSqlException("Exception while removing subscription with SQL "+e.getMessage(), e);
        }
    }
}
