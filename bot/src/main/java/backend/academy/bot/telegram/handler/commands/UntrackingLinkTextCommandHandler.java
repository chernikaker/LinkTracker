package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.LINK_NOT_VALID;
import static backend.academy.bot.telegram.handler.Constant.LINK_REMOVED_SUCCESS;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.NO_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.REQUEST_CANCELLED;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.validator.LinkUrlValidator;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

/** Обработчик текста ссылки при ее удалении */
public class UntrackingLinkTextCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public UntrackingLinkTextCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        if (!LinkUrlValidator.isValid(message.text())) {
            // проверка формата ссылки
            return LINK_NOT_VALID;
        }
        // удаление из кэша
        repository.removeTrack(message.chat().id());
        try {
            // удаление из Scrapper
            service.removeLinkSubscription(message.chat().id(), message.text());
            return LINK_REMOVED_SUCCESS;
        } catch (BotRequestException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionMessage().contains(message.chat().id() + " not exists")) {
                // пользователь не зарегистрирован
                return NOT_REGISTERED;
            }
            if (response.code().equals("404")) {
                // нет подписки на данную ссылку
                return NO_SUBSCRIPTION;
            }
            // ошибка, не зависящая от пользователя
            return REQUEST_CANCELLED;
        }
    }

    @Override
    public boolean canHandle(Message message) {
        // не поддерживает никакие команды
        if (message.text().startsWith("/")) {
            return false;
        }
        // проверка, что запись есть в кэше и состояние пользователя соответствующее
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.orElseThrow().state() == UserState.UNTRACKING_LINK;
    }
}
