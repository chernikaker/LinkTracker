package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import java.util.Map;

/** Контракт сервиса, управляющего подписками в БД */
public interface SubscriptionService {

    /** добавление подписки на ссылку (опционально с тегами и фильтрами) */
    long addSubscriptionOnLink(User user, Link link, List<Tag> tags, List<Filter> filters);

    /** получение списка подписок и их id для пользователя */
    Map<Long, Subscription> getUserSubscriptions(User user);

    /** получение списка подписок по id ссылки */
    List<Subscription> getSubscriptionsByLinkId(long linkId);

    /** удаление подписки пользователя на ссылку с получением информации о ней */
    Map.Entry<Long, Subscription> deleteSubscriptionByUserAndLink(User user, Link link);

    /** удаление подписок пользователя по тегу с получением информации о них */
    Map<Long, Subscription> deleteSubscriptionsByUserAndTag(User user, Tag tag);

    /** получение подписок пользователя по тегу */
    Map<Long, Subscription> getSubscriptionsByUserAndTag(User user, Tag tag);
}
