package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.orm.entity.OrmFilter;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
            checkExistingSubscription(u, link.url());
            OrmLink l = tryGetLinkByUrl(link.url());
            OrmSubscription sub = new OrmSubscription(l,u);
            List<OrmTag> ormTags = new ArrayList<>();
            for (Tag t : tags) {
                ormTags.add(createOrGetOrmTag(t));
            }
            List<OrmFilter> ormFilters = new ArrayList<>();
            for (Filter f : filters) {
                OrmFilter ormFilter = mapOrmFilter(f);
                ormFilter.subscription(sub);
                ormFilter.owner(u);
                ormFilters.add(ormFilter);
            }
            sub.tags(ormTags);
            sub.filters(ormFilters);
            subscrRepo.save(sub);
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
            for(OrmSubscription sub : u.subscriptions()){
                subscriptions.put(sub.id(), mapFromOrmSubscription(sub));
            }
            return subscriptions;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting user subscriptions with ORM", e);
        }
    }

    @Override
    @Transactional
    public List<Long> getLinkSubscribersChatsById(long linkId) {
        try {
            OrmLink link = tryGetLinkById(linkId);
            return link.subscriptions().stream()
                .map(s -> s.user().chatId())
                .toList();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while getting link subscribers with ORM", e);
        }
    }

    @Override
    @Transactional
    public Map.Entry<Long, Subscription> deleteSubscriptionByUserAndLink(User user, Link link) {
        try {
            OrmUser u = tryGetUserByChatId(user.chatId());
            OrmLink l = tryGetLinkByUrl(link.url());
            OrmSubscription sub = u.subscriptions().stream()
                .filter(s -> s.link().url().equals(link.url()))
                .findFirst()
                .orElseThrow(() -> new ScrapperSubscriptionNotExistsException("No subscription for user " + user.chatId()));
            Map.Entry<Long, Subscription> response = Map.entry(sub.id(), mapFromOrmSubscription(sub));
            subscrRepo.delete(sub);
            if(l.subscriptions().isEmpty()) {
                linkRepo.delete(l);
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while deleting subscription with ORM", e);
        }
    }


    private OrmUser tryGetUserByChatId(long chatId){
        return userRepo.findByChatId(chatId)
            .orElseThrow(() -> new ScrapperUserNotExistsException("User " + chatId + " does not exist"));
    }

    private OrmLink tryGetLinkByUrl(String url){
        return linkRepo.findByUrl(url)
            .orElseGet(() -> {
                OrmLink newLink = new OrmLink();
                newLink.url(url);
                return linkRepo.save(newLink);
            });
    }

    private OrmLink tryGetLinkById(long id){
        return linkRepo.findById(id)
            .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + id + " does not exist"));
    }

    private void checkExistingSubscription(OrmUser u, String url) {
        u.subscriptions().stream()
            .filter(s -> s.link().url().equals(url))
            .forEach(s -> {
                throw new ScrapperSubscriptionAlreadyExistsException("Subscription for user " + u.chatId() + "on link " + url + " already exists");
            });
    }

    private OrmTag mapOrmTag(Tag tag) {
        OrmTag newTag = new OrmTag();
        newTag.tagText(tag.value());
        return newTag;
    }

    private Tag mapFromOrmTag(OrmTag ormTag) {
        return new Tag(ormTag.tagText());
    }

    private Filter mapFromOrmFilter(OrmFilter ormFilter) {
        return new Filter(ormFilter.key(), ormFilter.value());
    }

    private Link mapFromOrmLink(OrmLink ormLink) {
        return new Link(ormLink.url(), LinkType.fromValue(ormLink.url()), ormLink.lastValidation());
    }

    private User mapFromOrmUser(OrmUser ormUser) {
        return new User(ormUser.chatId());
    }

    private Subscription mapFromOrmSubscription(OrmSubscription ormSubscr) {
        List<Tag> tags = ormSubscr.tags().stream().map(this::mapFromOrmTag).toList();
        List<Filter> filters = ormSubscr.filters().stream().map(this::mapFromOrmFilter).toList();
        return new Subscription(
            mapFromOrmUser(ormSubscr.user()),
            mapFromOrmLink(ormSubscr.link()),
            tags,
            filters
        );
    }

    private OrmFilter mapOrmFilter(Filter f) {
        OrmFilter newFilter = new OrmFilter();
        newFilter.key(f.key());
        newFilter.value(f.value());
        return newFilter;
    }

    private OrmTag createOrGetOrmTag(Tag t) {
        Optional<OrmTag> tag = tagRepo.findByTagText(t.value());
        return tag.orElseGet(() -> mapOrmTag(t));
    }
}
