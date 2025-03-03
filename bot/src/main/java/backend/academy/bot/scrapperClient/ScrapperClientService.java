package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.scrapperClient.BotInvalidLinkRequestException;
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
public class ScrapperClientService {

    private ScrapperClient client;

    public void registerNewClient(long chatId) {
        try {
            client.registerChat(chatId);
            log.info("Successfully registered new client with chat id {}", chatId);
        } catch (HttpClientErrorException e) {
            handleException(e);
        }
    }

    public ListLinksResponse getUserLinks(long chatId) {
        try {
            return client.getUserLinks(chatId);
        } catch (HttpClientErrorException e) {
            handleException(e);
            throw new BotInvalidLinkRequestException(e.getResponseBodyAs(ApiErrorResponse.class));
        }
    }

    public void addLinkSubscription(long chatId, LinkTrackingObject linkData) {
        try {
            AddLinkRequest request = new AddLinkRequest(
                    linkData.link(), Arrays.asList(linkData.tags()), Arrays.asList(linkData.filters()));
            client.addLinkSubscription(chatId, request);
        } catch (HttpClientErrorException e) {
            handleException(e);
        }
    }

    public void removeLinkSubscription(long chatId, String link) {
        try {
            RemoveLinkRequest request = new RemoveLinkRequest(link);
            client.deleteLinkSubscription(chatId, request);
        } catch (HttpClientErrorException e) {
            handleException(e);
        }
    }

    private void handleException(HttpClientErrorException e) {
        ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
        if (error != null) {
            log.error("Error while sending link update: {}", error.exceptionMessage());
        } else {
            log.error("Received a null error response.");
        }
        throw new BotInvalidLinkRequestException(error);
    }
}
