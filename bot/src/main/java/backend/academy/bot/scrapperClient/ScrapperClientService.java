package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.custom.scrapperClient.BotChatRegistrationException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidChatIdException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;


@AllArgsConstructor
@Slf4j
public class ScrapperClientService {

    private IClient client;

    public void registerNewClient(long chatId) {
        try {
            client.registerChat(chatId);
            log.info("Successfully registered new client with chat id " + chatId);
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            log.error("Scrapper client exception: "+error.exceptionMessage());
            throw new BotChatRegistrationException(error);
        }
    }

    public ListLinksResponse getUserLinks(long chatId) {
        try {
            return client.getUserLinks(chatId);
        }  catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            log.error("Scrapper client exception: "+error.exceptionMessage());
            throw new BotInvalidChatIdException(error);
        }
    }

    public void addLinkSubscription (long chatId, LinkTrackingObject linkData) {
        try {
            AddLinkRequest request = new AddLinkRequest(
                linkData.link(),
                Arrays.asList(linkData.tags()),
                Arrays.asList(linkData.filters())
            );
            client.addLinkSubscription(chatId, request);
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            log.error("Scrapper client exception: "+error.exceptionMessage());
            throw new BotInvalidLinkRequestException(error);
        }
    }

    public void removeLinkSubscription(long chatId, String link) {
        try {
            RemoveLinkRequest request = new RemoveLinkRequest(link);
            client.deleteLinkSubscription(chatId, request);
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            log.error("Scrapper client exception: "+error.exceptionMessage());
            throw new BotInvalidLinkRequestException(error);
        }
    }
}
