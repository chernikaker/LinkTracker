package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.custom.scrapperClient.BotChatRegistrationException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidChatIdException;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


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
