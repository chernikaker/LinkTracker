package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import java.util.List;
import java.util.Map;

/** Контракт сервиса, управляющего тегами в БД */
public interface TagService {

    /** Добавление тегов для подписки пользователя на ссылку и возвращение информации о них */
    Map<Long, Tag> addTagsForUserAndLink(User user, Link link, List<Tag> tags);

    /** Добавление тега пользователя и возвращение информации о нем */
    Map.Entry<Long, Tag> deleteTagForUser(User user, Tag tag);

    /** Открепление тега от подписки пользователя на ссылку и возвращение информации о нем */
    Map.Entry<Long, Tag> deleteTagForSubscriptionData(User user, Link link, String text);

    /** Получение тегов для подписки по id */
    List<Tag> getTagsBySubscriptionId(long id);

    /** Получение тегов и их идентификаторов для пользователя */
    Map<Long, Tag> getTagsForUser(User user);
}
