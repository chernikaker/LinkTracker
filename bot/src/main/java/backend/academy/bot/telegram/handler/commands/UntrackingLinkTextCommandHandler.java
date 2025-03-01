package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class UntrackingLinkTextCommandHandler extends CommandHandler{

    private final ScrapperClientService service;

    public UntrackingLinkTextCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    protected String processRequest(Message message) {
        repository.removeTrack(message.chat().id());
        // TODO: validation
        try {
            service.removeLinkSubscription(message.chat().id(), message.text());
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
                return "Запрос отклонен, попробуйте ещё раз";
            }
        }
    }

    @Override
    public boolean canHandle(Message message) {
        if(message.text().startsWith("/")) {
            return false;
        }
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.get().state() == UserState.UNTRACKING_LINK;
    }
}
