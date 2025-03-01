package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidChatIdException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Message;

public class ListCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public ListCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    protected String processRequest(Message message) {
        try {
            ListLinksResponse list = service.getUserLinks(message.chat().id());
            return makeLinksMessage(list);
        }  catch (BotInvalidChatIdException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionMessage().contains("not exists")) {
                return "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
            } else {
                return "Запрос отклонен, попробуйте ещё раз";
            }
        }
    }

    @Override
    public boolean canHandle(Message message) {
        return !repository.containsTrack(message.chat().id())&&message.text().equals("/list");
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
