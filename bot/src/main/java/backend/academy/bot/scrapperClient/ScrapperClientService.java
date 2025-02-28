package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.custom.scrapperClient.BotChatRegistrationException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidChatIdException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Arrays;


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

    public String addLinkSubscription (long chatId, LinkTrackingObject linkData) {
        try {
            AddLinkRequest request = new AddLinkRequest(
                linkData.link(),
                Arrays.asList(linkData.tags()),
                Arrays.asList(linkData.filters())
            );
            LinkResponse response = client.addLinkSubscription(chatId, request);
            return "Ссылка успешно зарегистрирована!";
        } catch (BotInvalidLinkRequestException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionName().equals("ScrapperUserNotExistsException")) {
                return "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
            }
            if(response.exceptionMessage().contains("Validation failed for argument [1]")) {
                return "Введена невалидная ссылка. Запрос отклонен, попробуйте ещё раз";
            } else {
                log.error("Scrapper client exception: "+ex.getMessage());
                return "Запрос отклонен, попробуйте ещё раз";
            }
        }
    }

    public String untrackLinkSubscription (long chatId, String link) {
        try {
            RemoveLinkRequest request = new RemoveLinkRequest(link);
            client.deleteLinkSubscription(chatId, request);
            return "Ссылка успешно удалена";
        } catch (BotInvalidLinkRequestException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionName().equals("ScrapperUserNotExistsException")) {
                return "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
            }
            if(response.exceptionMessage().contains("Validation failed for argument [1]")) {
                return "Введена невалидная ссылка. Запрос отклонен, попробуйте ещё раз";
            } if(response.code().equals("404")) {
                return "У вас нет подписки на данную ссылку";
            } else {
                log.error("Scrapper client exception: "+ex.getMessage());
                return "Запрос отклонен, попробуйте ещё раз";
            }
        }
    }
}
