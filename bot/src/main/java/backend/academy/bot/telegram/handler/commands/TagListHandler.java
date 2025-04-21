package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.EXTERNAL_ERROR;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.NO_TAGS;
import static backend.academy.bot.telegram.handler.Constant.TAG_HEADER;
import static backend.academy.bot.telegram.handler.Constant.UNKNOWN_ERROR;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.TagResponse;
import com.pengrad.telegrambot.model.Message;
import org.springframework.http.HttpStatus;

/** Обработчик команды получения списка тегов */
public class TagListHandler extends CommandHandler {

    private final ScrapperClientService service;

    public TagListHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        try {
            // получение тегов из Scrapper
            ListTagsResponse list = service.getUserTags(message.chat().id());
            return makeTagsMessage(list);
        } catch (BotRequestException ex) {
            return getErrorMessage(ex.response());
        }
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если пользователь ничего не вводит
        // и команда равна /tags
        return !repository.containsTrack(message.chat().id()) && message.text().equals(Command.TAGS.command());
    }

    private String makeTagsMessage(ListTagsResponse response) {
        if (response.size() == 0) {
            return NO_TAGS;
        }
        StringBuilder tags = new StringBuilder(TAG_HEADER);
        for (TagResponse tag : response.tags()) {
            tags.append("#").append(tag.value()).append('\n');
        }
        return tags.toString();
    }

    private String getErrorMessage(ApiErrorResponse response) {
        if (response == null) {
            return UNKNOWN_ERROR;
        }
        if (response.code().equals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))) {
            return EXTERNAL_ERROR;
        }
        if (response.exceptionName().contains("UserNotExist")) {
            return NOT_REGISTERED;
        }
        return UNKNOWN_ERROR;
    }
}
