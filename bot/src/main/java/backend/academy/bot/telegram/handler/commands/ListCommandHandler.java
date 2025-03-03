package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.FILTER_HEADER;
import static backend.academy.bot.telegram.handler.Constant.LINK_HEADER;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.NO_LINKS;
import static backend.academy.bot.telegram.handler.Constant.REQUEST_CANCELLED;
import static backend.academy.bot.telegram.handler.Constant.TAG_HEADER;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotInvalidChatIdException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Message;

/** Обработчик команды /list */
public class ListCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public ListCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        try {
            // успешное получение ссылок
            ListLinksResponse list = service.getUserLinks(message.chat().id());
            return makeLinksMessage(list);
        } catch (BotInvalidChatIdException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionMessage().contains("not exists")) {
                // пользователя не существует
                return NOT_REGISTERED;
            } else {
                // ошибка, не зависящая от пользователя
                return REQUEST_CANCELLED;
            }
        }
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если пользователь ничего не вводит
        // и команда равна /list
        return !repository.containsTrack(message.chat().id()) && message.text().equals(Command.LIST.command());
    }

    /**
     * Метод формирует сообщение о ссылках
     *
     * @param links DTO с ссылками
     * @return сообщение пользователю
     */
    private String makeLinksMessage(ListLinksResponse links) {
        if (links.size() == 0) {
            return NO_LINKS;
        }
        StringBuilder sb = new StringBuilder(LINK_HEADER);
        for (LinkResponse link : links.links()) {
            sb.append(link.url()).append("\n");
            if (!link.tags().isEmpty()) {
                sb.append(TAG_HEADER);
                for (String tag : link.tags()) {
                    sb.append(tag).append("\n");
                }
            }
            if (!link.filters().isEmpty()) {
                sb.append(FILTER_HEADER);
                for (String filter : link.filters()) {
                    sb.append(filter).append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
