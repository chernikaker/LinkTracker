package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidLinkRequestException;
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
    protected String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking = repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.get();
        String writingResponse = writeFilters(message.text(), tracking);
        try {
            service.addLinkSubscription(message.chat().id(), tracking);
            repository.removeTrack(message.chat().id());
            return writingResponse + "\nСсылка успешно зарегистрирована!";
        } catch (BotInvalidLinkRequestException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionName().equals("ScrapperUserNotExistsException")) {
                return "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
            }
            if(response.exceptionMessage().contains("Validation failed for argument [1]")) {
                return "Введена невалидная ссылка. Запрос отклонен, попробуйте ещё раз";
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
        return link.isPresent() && link.get().state() == UserState.TRACKING_FILTER;
    }

    private String writeFilters(String filter, LinkTrackingObject tracking) {
        String message = "Фильтры не установлены";
        String[] filters = new String[0];
        if(!"-".equals(filter)){
            filters = filter.split(" ");
            message = "Фильтры установлены";
        }
        tracking.filters(filters);
        tracking.state(UserState.DEFAULT);
        return message;
    }
}
