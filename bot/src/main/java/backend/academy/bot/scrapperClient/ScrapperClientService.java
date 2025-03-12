package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import java.util.Arrays;
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
