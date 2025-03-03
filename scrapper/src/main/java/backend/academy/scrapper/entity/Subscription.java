package backend.academy.scrapper.entity;

import java.util.List;

/**
 * Сущность подписки пользователя на ссылку
 *
 * @param userId id подписанного пользователя в репозитории
 * @param user объект подписанного пользователя (для удобства)
 * @param linkId id ссылки подписки в репозитории
 * @param link объект ссылки подписки (для удобства)
 * @param tags теги подписки
 * @param filters фильтры подписки
 */
public record Subscription(long userId, User user, long linkId, Link link, List<String> tags, List<String> filters) {}
