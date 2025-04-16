package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.mapper.OrmTagMapper;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class OrmTagService implements TagService {

    private final OrmUserRepository userRepo;
    private final OrmSubscriptionRepository subscrRepo;
    private final OrmTagRepository tagRepo;

    @Override
    @Transactional
    public void addTagsForUserAndLink(User user, Link link, List<Tag> tags) {
        try {
            OrmUser ormUser = tryGetUserByChatId(user.chatId());
            OrmSubscription sub = ormUser.subscriptions().stream()
                .filter(s -> s.link().url().equals(link.url()))
                .findFirst().orElseThrow(
                    () -> new ScrapperSubscriptionNotExistsException("No subscription found for " + link.url())
                );
            for(Tag tag : tags) {
                Optional<OrmTag> existingTag = ormUser.tags().stream()
                    .filter(t -> t.tagText().equals(tag.value()))
                    .findFirst();
                OrmTag t = existingTag.orElseGet(() -> OrmTagMapper.mapToOrm(tag));
                if(!sub.tags().contains(t)) {
                    sub.tags().add(t);
                    t.subscriptions(new ArrayList<>(List.of(sub)));
                    if(existingTag.isEmpty()) {
                        ormUser.tags().add(t);
                        t.owner(ormUser);
                    }
                    tagRepo.save(t);
                    tagRepo.flush();
                }
            }
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while adding  ORM tags to " + user.chatId(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForUser(User user, Tag tag) {
        try {
            OrmUser ormUser = tryGetUserByChatId(user.chatId());
            OrmTag ormTag = ormUser.tags().stream()
                    .filter(t -> t.tagText().equals(tag.value()))
                    .findFirst()
                    .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + tag.value() + " not found"));
            ormUser.tags().remove(ormTag);
            tagRepo.delete(ormTag);
            tagRepo.flush();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while deleting with ORM tag " + tag.value(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTagForSubscriptionData(User user, Link link, String text) {
        try {
            OrmUser subscriber = tryGetUserByChatId(user.chatId());
            OrmSubscription sub = subscriber.subscriptions().stream()
                    .filter(s -> s.link().url().equals(link.url()))
                    .findFirst()
                    .orElseThrow(() ->
                            new ScrapperSubscriptionNotExistsException("Subscription " + link.url() + " not found"));
            OrmTag tag = sub.tags().stream()
                    .filter(t -> t.tagText().equals(text))
                    .findFirst()
                    .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + text + " not found"));
            sub.tags().remove(tag);
            tag.subscriptions().remove(sub);
            subscrRepo.save(sub);
            subscrRepo.flush();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while deleting with ORM subscription " + link.url(), e);
        }
    }

    @Override
    @Transactional
    public List<Tag> getTagsBySubscriptionId(long id) {
        try {
            OrmSubscription sub = subscrRepo
                    .findById(id)
                    .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("Subscription " + id + " not found"));
            return sub.tags().stream().map(OrmTagMapper::mapFromOrm).toList();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting tags with ORM by subscription " + id, e);
        }
    }

    @Override
    public List<Tag> getTagsForUser(User user) {
        try {
            OrmUser ormUser = tryGetUserByChatId(user.chatId());
            return ormUser.tags().stream().map(OrmTagMapper::mapFromOrm).toList();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting tags for user " + user.chatId(), e);
        }
    }

    private OrmUser tryGetUserByChatId(long chatId) {
        return userRepo.findByChatId(chatId)
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

}
