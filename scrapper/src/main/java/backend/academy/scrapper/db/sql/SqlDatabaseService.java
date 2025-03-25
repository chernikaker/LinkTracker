package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.DatabaseService;
import backend.academy.scrapper.db.sql.entity.SqlFilter;
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
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
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
    public void addSubscriptionOnLink(User user, Link link) {
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            Optional<Link> existingLink = linkRepo.findLinkByUrl(link.url());
            long linkId = existingLink.map(Link::id).orElseGet(() -> linkRepo.addLink(link));

            subscrRepo.addSubscription(new SqlSubscription(u.id(), linkId));
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding link with SQL", e);
        }
    }

    @Override
    @Transactional
    public void removeSubscriptionOnLink(User user, Link link) {
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            Link l = linkRepo.findLinkByUrl(link.url())
                .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + link.url() + " does not exist"));
            SqlSubscription existingSub = subscrRepo.getSubscriptionByLinkAndUserId(l.id(), u.id())
                .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription " + link.url() + " does not exist"));
            subscrRepo.deleteSubscriptionById(existingSub.id());
            List<SqlSubscription> subs = subscrRepo.getSubscriptionsByLink(l.id());
            if(subs.isEmpty()){
                linkRepo.deleteLinkById(l.id());
            }
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
        Link link = linkRepo.findLinkByUrl(subscription.link().url())
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
    public List<Link> getLinksToCheck(int batchSize, long offset, long duration){
        try {
            return linkRepo.getUncheckedLinksWithBatching(batchSize, offset, duration);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting links to check", e);
        }
    }


    public void updateLinkValidationWithTime(Link link, LocalDateTime time) {
        try {
            linkRepo.updateLinkValidationById(link.id(), time);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while updating link validation", e);
        }
    }

    @Override
    public void updateLinkValidationNow(Link link){
        updateLinkValidationWithTime(link, LocalDateTime.now());
    }

    @Override
    @Transactional
    public List<Subscription> getUserLinks(User user){
        try {
            SqlUser u = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            List<Subscription> ans = new ArrayList<>();
            for (SqlSubscription s :subscrRepo.getSubscriptionsByUserId(u.id())){
                Link l = linkRepo.getLinkById(s.linkId());
                List<Tag> tags = new ArrayList<>();
                for (SqlTag t: tagRepo.getSubscriptionTags(s.id())) {
                    tags.add(new Tag(t.tagText()));
                }
                List<Filter> filters = new ArrayList<>();
                for (SqlFilter f: filterRepo.getFiltersBySubscriptionId(s.id())) {
                    filters.add(new Filter(f.key(), f.value()));
                }
                ans.add(new Subscription(user, l, tags, filters));
            }
            return ans;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting user links", e);
        }
    }
}
