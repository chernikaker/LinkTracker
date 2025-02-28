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
import java.util.Arrays;


@AllArgsConstructor
@Slf4j
public class ScrapperClientService {

    private IClient client;

    public String registerNewClient(long chatId) {
        try {
            client.registerChat(chatId);
            log.info("Successfully registered new client with chat id " + chatId);
            return "Чат успешно зарегистрирован";
        } catch (BotChatRegistrationException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionName().equals("ScrapperUserAlreadyExistsException")) {
                return "Вы уже зарегистрированы";
            } else {
                log.error("Scrapper client exception: "+ex.getMessage());
                return "Регистрация отклонена, попробуйте ещё раз";
            }
        }
    }

    public String getUserLinks(long chatId) {
        try {
            ListLinksResponse response = client.getUserLinks(chatId);
            return makeLinksMessage(response);
        } catch (BotInvalidChatIdException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionName().equals("ScrapperUserNotExistsException")) {
                return "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
            } else {
                log.error("Scrapper client exception: "+ex.getMessage());
                return "Запрос отклонен, попробуйте ещё раз";
            }
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

    private String makeLinksMessage(ListLinksResponse links) {
        if(links.size() == 0) {
            return "Отслеживаемых ссылок нет";
        }
        StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n\n");
        for (LinkResponse link : links.links()) {
            sb.append(link.url()).append("\n");
            if(!link.tags().isEmpty()) {
                sb.append("Теги: \n");
                for(String tag : link.tags()) {
                    sb.append(tag).append("\n");
                }
            }
            if(!link.filters().isEmpty()) {
                sb.append("Фильтры\n");
                for(String filter : link.filters()) {
                    sb.append(filter).append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
