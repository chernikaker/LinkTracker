package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.TagService;
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
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.ArrayList;
import java.util.List;
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
}
