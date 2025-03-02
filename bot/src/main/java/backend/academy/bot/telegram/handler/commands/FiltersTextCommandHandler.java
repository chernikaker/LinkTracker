package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class FiltersTextCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public FiltersTextCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking =
                repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.orElseThrow();
        String writingResponse = writeFilters(message.text(), tracking);
        try {
            repository.removeTrack(message.chat().id());
            service.addLinkSubscription(message.chat().id(), tracking);
            return writingResponse + "\nСсылка успешно зарегистрирована!";
        } catch (BotInvalidLinkRequestException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionMessage().contains(message.chat().id() + " not exists")) {
                return "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
            }
            if (response.exceptionMessage().contains("Validation failed for argument [1]")) {
                return "Введена невалидная ссылка. Запрос отклонен, попробуйте ещё раз";
            }
            if (response.exceptionMessage().contains("unavailable")) {
                return "Введенная ссылка недоступна, запрос отклонен";
            }
            return "Запрос отклонен, попробуйте ещё раз";
        }
    }

    @Override
    public boolean canHandle(Message message) {
        if (message.text().startsWith("/")) {
            return false;
        }
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.orElseThrow().state() == UserState.TRACKING_FILTER;
    }

    private String writeFilters(String filter, LinkTrackingObject tracking) {
        String message = "Фильтры не установлены";
        String[] filters = new String[0];
        if (!"-".equals(filter)) {
            filters = filter.split(" ");
            message = "Фильтры установлены";
        }
        tracking.filters(filters);
        tracking.state(UserState.DEFAULT);
        return message;
    }
}
