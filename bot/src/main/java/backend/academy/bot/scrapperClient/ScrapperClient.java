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

/** Контракт для клиента Scrapper. Методы, принимаемые и возвращаемые значения соответствуют контракту OpenAPI */
public interface ScrapperClient {

    void registerChat(long userId);

    ListLinksResponse getUserLinks(long userId);

    LinkResponse addLinkSubscription(long userId, AddLinkRequest request);

    void deleteLinkSubscription(long userId, RemoveLinkRequest request);

    ListLinkTagsResponse addTagsToSubscription(long userId, AddLinkTagsRequest request);

    LinkTagResponse removeTagFromSubscription(long userId, RemoveLinkTagRequest request);

    ListTagLinksResponse removeSubscriptionsByTag(long userId, String tag);

    TagResponse deleteTag(long userId, RemoveTagRequest request);

    ListTagLinksResponse getTagSubscriptions(long userId, String tag);

    ListTagsResponse getTags(long userId);
}
