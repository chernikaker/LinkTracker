package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.AddLinkTagsRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.RemoveLinkRequest;
import java.util.Arrays;
import java.util.List;
import backend.academy.dto.RemoveLinkTagRequest;
import backend.academy.dto.RemoveTagRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Класс обёртка для удобной работы со ScrapperClient. Преобразует внутренние модели объектов в DTO, обрабатывает и
 * логирует исключения клиента.
 */
@AllArgsConstructor
@Slf4j
@Service
public final class ScrapperClientService {

    private ScrapperClient client;

    public void registerNewClient(long chatId) {
        try {
            client.registerChat(chatId);
            log.atInfo().addKeyValue("chat id", chatId).log("Client added successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public ListLinksResponse getUserLinks(long chatId) {
        try {
            ListLinksResponse response = client.getUserLinks(chatId);
            log.atInfo().addKeyValue("chat id", chatId).log("Links retrieved successfully");
            return response;
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public void addLinkSubscription(long chatId, LinkTrackingObject linkData) {
        try {
            AddLinkRequest request = new AddLinkRequest(
                    linkData.link(), Arrays.asList(linkData.tags()), Arrays.asList(linkData.filters()));
            client.addLinkSubscription(chatId, request);
            log.atInfo().addKeyValue("chat id", chatId).log("Subscription added successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public void removeLinkSubscription(long chatId, String link) {
        try {
            RemoveLinkRequest request = new RemoveLinkRequest(link);
            client.deleteLinkSubscription(chatId, request);
            log.atInfo().addKeyValue("chat id", chatId).log("Subscription removed successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public void addTagsToSubscription(long chatId, LinkTrackingObject data) {
        try {
            AddLinkTagsRequest request = new AddLinkTagsRequest(data.link(), List.of(data.tags()));
            client.addTagsToSubscription(chatId, request);
            log.atInfo().addKeyValue("chat id", chatId).log("Tags added successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public void removeTagBySubscription(long chatId, LinkTrackingObject data) {
        try {
            RemoveLinkTagRequest request = new RemoveLinkTagRequest(data.link(), data.tags()[0]);
            client.removeTagFromSubscription(chatId, request);
            log.atInfo().addKeyValue("chat id", chatId).log("Tag was removed from subscription successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public void removeSubscriptionsByTag(long chatId, String tag) {
        try {
            client.removeSubscriptionsByTag(chatId, tag);
            log.atInfo().addKeyValue("chat id", chatId).log("Subscriptions removed by tag successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public void deleteTag(long chatId, String tag) {
        try {
            RemoveTagRequest request = new RemoveTagRequest(tag);
            client.deleteTag(chatId, request);
            log.atInfo().addKeyValue("chat id", chatId).log("Tag removed successfully");
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public ListTagLinksResponse getSubscriptionsByTag(long chatId, String tag) {
        try {
            ListTagLinksResponse response = client.getTagSubscriptions(chatId, tag);
            log.atInfo().addKeyValue("chat id", chatId).log("Tag subscriptions were sent successfully");
            return response;
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    public ListTagsResponse getUserTags(long chatId){
        try {
            ListTagsResponse response = client.getTags(chatId);
            log.atInfo().addKeyValue("chat id", chatId).log("Tags were sent successfully");
            return response;
        } catch (HttpClientErrorException e) {
            ApiErrorResponse r = handleException(e);
            throw new BotRequestException(r);
        }
    }

    private ApiErrorResponse handleException(HttpClientErrorException e) {
        ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
        if (error != null) {
            log.atWarn()
                    .addKeyValue("error response", e.getResponseBodyAsString())
                    .log("Error while sending link update");
        } else {
            log.atWarn().log("Error while sending link update: null error response");
        }
        return error;
    }
}
