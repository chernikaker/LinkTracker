package backend.academy.bot.scrapperClient;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.AddLinkTagsRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkTagResponse;
import backend.academy.dto.ListLinkTagsResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.dto.RemoveLinkTagRequest;
import backend.academy.dto.RemoveTagRequest;
import backend.academy.dto.TagResponse;

/**
 * Контракт для клиента Scrapper. Методы, принимаемые и возвращаемые значения соответствуют контракту OpenAPI. Т Также
 * добавлены новые методы для работы с тегами
 */
public interface ScrapperClient {

    void registerChat(long userId);

    ListLinksResponse getUserLinks(long userId);

    LinkResponse addLinkSubscription(long userId, AddLinkRequest request);

    void deleteLinkSubscription(long userId, RemoveLinkRequest request);

    /** Метод добавления тега (существующего или нового к существующей подписке пользователя) */
    ListLinkTagsResponse addTagsToSubscription(long userId, AddLinkTagsRequest request);

    /** Метод открепления тега от существующей подписки пользователя */
    LinkTagResponse removeTagFromSubscription(long userId, RemoveLinkTagRequest request);

    /** Метод удаления всех подписок, помеченных данным тегом */
    ListTagLinksResponse removeSubscriptionsByTag(long userId, String tag);

    /** Метод удаления тега */
    TagResponse deleteTag(long userId, RemoveTagRequest request);

    /** Метод получения всех подписок, помеченных данным тегом */
    ListTagLinksResponse getTagSubscriptions(long userId, String tag);

    /** Метод получения всех тегов */
    ListTagsResponse getTags(long userId);
}
