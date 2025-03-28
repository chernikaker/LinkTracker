package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@AllArgsConstructor
public class OrmTagService implements TagService {

    private final OrmUserRepository userRepo;
    private final OrmSubscriptionRepository subscrRepo;
    private final OrmTagRepository tagRepo;

    @Override
    @Transactional
    public void deleteTagForUser(User user, Tag tag) {
        try {
            OrmUser ormUser = tryGetUserByChatId(user.chatId());
            OrmTag ormTag = ormUser.tags().stream()
                .filter(t -> t.tagText().equals(tag.value()))
                .findFirst()
                .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + tag.value() + " not found"));
            tagRepo.delete(ormTag);
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while deleting with ORM tag " + tag.value(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForSubscription(Subscription subscription, String text) {
        try {
            OrmUser subscriber = tryGetUserByChatId(subscription.user().chatId());
            OrmSubscription sub = subscriber.subscriptions().stream()
                .filter(s -> s.link().url().equals(subscription.link().url()))
                .findFirst()
                .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription " + subscription.link().url() + " not found"));
            sub.tags().stream().filter(t -> t.tagText().equals(text)).forEach(sub.tags()::remove);
            subscrRepo.save(sub);
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while deleting with ORM subscription " + subscription.link().url(), e);
        }
    }

    @Override
    @Transactional
    public List<Tag> getTagsBySubscriptionId(long id) {
        try {
            OrmSubscription sub = subscrRepo.findById(id).orElseThrow(
                () -> new ScrapperSubscriptionNotExistsException("Subscription " + id + " not found")
            );
            return sub.tags().stream()
                .map(this::mapFromOrmTag)
                .toList();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting tags with ORM by subscription " + id, e);
        }
    }

    private OrmUser tryGetUserByChatId(long chatId){
        return userRepo.findByChatId(chatId)
            .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

    private Tag mapFromOrmTag(OrmTag ormTag) {
        return new Tag(ormTag.tagText());
    }
}
