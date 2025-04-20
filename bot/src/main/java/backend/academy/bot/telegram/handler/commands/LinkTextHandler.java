package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.EXTERNAL_ERROR;
import static backend.academy.bot.telegram.handler.Constant.LINK_NOT_VALID;
import static backend.academy.bot.telegram.handler.Constant.LINK_REMOVED_SUCCESS;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.NO_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.TAGS_TRACKING_MESSAGE;
import static backend.academy.bot.telegram.handler.Constant.TAG_TRACKING_MESSAGE;
import static backend.academy.bot.telegram.handler.Constant.UNKNOWN_ERROR;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.bot.validator.LinkUrlValidator;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;
import org.springframework.http.HttpStatus;
import java.util.Optional;

/** Обработчик текста ссылки при ее добавлении */

public class LinkTextHandler extends CommandHandler {

    private final ScrapperClientService service;

    public LinkTextHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        // записывает ссылку в соответствующий объект кэша
        Optional<LinkTrackingObject> potentialTracking =
                repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.orElseThrow();
        if (!LinkUrlValidator.isValid(message.text())) {
            return LINK_NOT_VALID;
        }
        if(tracking.command() == Command.UNTRACK) {
            return sendUntrackingRequest(message.text(), message.chat().id());
        }
        return writeLink(message.text(), tracking);
    }

    @Override
    public boolean canHandle(Message message) {
        // не поддерживает никакие команды
        if (message.text().startsWith("/")) {
            return false;
        }
        // проверка, что запись есть в кэше и состояние пользователя соответствующее
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.orElseThrow().state() == UserState.TRACKING_LINK;
    }

    private String writeLink(String link, LinkTrackingObject tracking) {
        tracking.link(link);
        tracking.state(UserState.TRACKING_TAG);
        if(tracking.command() == Command.REMOVE_TAG_SUB){
            return TAG_TRACKING_MESSAGE;
        }
        return TAGS_TRACKING_MESSAGE.formatted(EMPTY_INPUT);
    }

    private String sendUntrackingRequest(String message, Long chatId) {
        // удаление из кэша
        repository.removeTrack(chatId);
        try {
            // удаление из Scrapper
            service.removeLinkSubscription(chatId, message);
            return LINK_REMOVED_SUCCESS;
        } catch (BotRequestException ex) {
            return getErrorMessage(ex.response());
        }
    }

    private String getErrorMessage(ApiErrorResponse response) {
        if(response == null) {
            return UNKNOWN_ERROR;
        }
        if (response.code().equals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))) {
            return EXTERNAL_ERROR;
        }
        if(response.exceptionName().contains("UserNotExist")) {
            return NOT_REGISTERED;
        }
        if(response.code().equals(String.valueOf(HttpStatus.NOT_FOUND.value()))) {
            return NO_SUBSCRIPTION;
        }
        return UNKNOWN_ERROR;
    }
}
