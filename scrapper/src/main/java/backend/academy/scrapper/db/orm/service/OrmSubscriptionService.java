package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.orm.entity.OrmFilter;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.mapper.OrmFilterMapper;
import backend.academy.scrapper.db.orm.mapper.OrmLinkMapper;
import backend.academy.scrapper.db.orm.mapper.OrmSubscriptionMapper;
import backend.academy.scrapper.db.orm.mapper.OrmTagMapper;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
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
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

/** ORM реализация сервиса работы с подписками */
@AllArgsConstructor
public class OrmSubscriptionService implements SubscriptionService {

    private final OrmSubscriptionRepository subscrRepo;
    private final OrmLinkRepository linkRepo;
    private final OrmUserRepository userRepo;
    private final OrmTagRepository tagRepo;

    @Override
    @Transactional
    public long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters) {
        try {
            OrmUser u = tryGetUserByChatId(user.chatId());
            OrmLink l = tryGetLinkByData(link);
            // проверка на существование подписки
            checkExistingSubscription(u, link.url());
            OrmSubscription sub = new OrmSubscription(l, u);
            List<OrmTag> ormTags = new ArrayList<>();
            for (Tag t : tags) {
                OrmTag tag = createOrGetOrmTag(u, t);
                tag.owner(u);
                ormTags.add(tag);
            }
            List<OrmFilter> ormFilters = new ArrayList<>();
            for (Filter f : filters) {
                OrmFilter ormFilter = OrmFilterMapper.mapToOrm(f);
                ormFilter.subscription(sub);
                ormFilter.owner(u);
                ormFilters.add(ormFilter);
            }
            sub.tags(ormTags);
            sub.filters(ormFilters);
            sub = subscrRepo.save(sub);
            subscrRepo.flush();
            return sub.id();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while adding subscription with ORM on link " + link.url(), e);
        }
    }

    @Override
    @Transactional
    public Map<Long, Subscription> getUserSubscriptions(User user) {
        try {
            OrmUser u = tryGetUserByChatId(user.chatId());
            Map<Long, Subscription> subscriptions = new HashMap<>();
            for (OrmSubscription sub : u.subscriptions()) {
                subscriptions.put(sub.id(), OrmSubscriptionMapper.mapFromOrm(sub));
            }
            return subscriptions;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting user subscriptions with ORM", e);
        }
    }

    @Override
    @Transactional
    public List<Subscription> getSubscriptionsByLinkId(long linkId) {
        try {
            OrmLink l = tryGetLinkById(linkId);
            List<Subscription> subscriptions = new ArrayList<>();
            for (OrmSubscription sub : l.subscriptions()) {
                subscriptions.add(OrmSubscriptionMapper.mapFromOrm(sub));
            }
            return subscriptions;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting user subscriptions with ORM", e);
        }
    }

    @Override
    @Transactional
    public Map.Entry<Long, Subscription> deleteSubscriptionByUserAndLink(User user, Link link) {
        try {
            OrmUser u = tryGetUserByChatId(user.chatId());
            OrmSubscription sub = u.subscriptions().stream()
                    .filter(s -> s.link().url().equals(link.url()))
                    .findFirst()
                    .orElseThrow(() ->
                            new ScrapperSubscriptionNotExistsException("No subscription for user " + user.chatId()));
            Map.Entry<Long, Subscription> response = Map.entry(sub.id(), OrmSubscriptionMapper.mapFromOrm(sub));
            OrmLink l = sub.link();
            l.subscriptions().remove(sub);
            u.subscriptions().remove(sub);
            subscrRepo.delete(sub);
            if (l.subscriptions().isEmpty()) {
                // если у ссылки нет подписок, удаляем ее
                linkRepo.delete(l);
            }
            subscrRepo.flush();
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while deleting subscription with ORM", e);
        }
    }

    @Override
    @Transactional
    public Map<Long, Subscription> deleteSubscriptionsByUserAndTag(User user, Tag tag) {
        try {
            OrmUser u = tryGetUserByChatId(user.chatId());
            OrmTag t = u.tags().stream()
                    .filter(tg -> tag.value().equals(tg.tagText()))
                    .findFirst()
                    .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + tag.value() + " not found"));
            List<OrmSubscription> subs = t.subscriptions();
            var ans = subs.stream().collect(Collectors.toMap(OrmSubscription::id, OrmSubscriptionMapper::mapFromOrm));
            for (OrmSubscription sub : subs) {
                OrmLink l = sub.link();
                u.subscriptions().remove(sub);
                l.subscriptions().remove(sub);
                subscrRepo.delete(sub);
                if (l.subscriptions().isEmpty()) {
                    // если у ссылки нет подписок, удаляем ее
                    linkRepo.delete(l);
                }
            }
            subscrRepo.flush();
            return ans;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting subscriptions with ORM", e);
        }
    }

    @Override
    @Transactional
    public Map<Long, Subscription> getSubscriptionsByUserAndTag(User user, Tag tag) {
        try {
            OrmUser u = tryGetUserByChatId(user.chatId());
            OrmTag t = u.tags().stream()
                    .filter(tg -> tag.value().equals(tg.tagText()))
                    .findFirst()
                    .orElseThrow(() -> new ScrapperTagNotExistsException("Tag " + tag.value() + " not found"));
            return t.subscriptions().stream()
                    .collect(Collectors.toMap(OrmSubscription::id, OrmSubscriptionMapper::mapFromOrm));
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting subscriptions with ORM", e);
        }
    }

    private OrmUser tryGetUserByChatId(long chatId) {
        return userRepo.findByChatId(chatId)
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

    private OrmLink tryGetLinkByData(Link link) {
        // если ссылки еще нет в БД, добавляем ее
        return linkRepo.findByUrl(link.url()).orElseGet(() -> {
            OrmLink newLink = OrmLinkMapper.mapToOrm(link);
            return linkRepo.save(newLink);
        });
    }

    private OrmLink tryGetLinkById(long id) {
        return linkRepo.findById(id)
                .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + id + " does not exist"));
    }

    private void checkExistingSubscription(OrmUser u, String url) {
        if (u.subscriptions().stream().anyMatch(s -> s.link().url().equals(url))) {
            throw new ScrapperSubscriptionAlreadyExistsException("Subscription " + url + " already exists");
        }
    }

    private OrmTag createOrGetOrmTag(OrmUser u, Tag t) {
        Optional<OrmTag> tag = tagRepo.findByTagTextAndOwner(t.value(), u);
        return tag.orElseGet(() -> OrmTagMapper.mapToOrm(t));
    }
}
