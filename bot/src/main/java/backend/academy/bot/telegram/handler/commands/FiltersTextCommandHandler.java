package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.FILTERS_NOT_REGISTERD;
import static backend.academy.bot.telegram.handler.Constant.FILTERS_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.LINK_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.LINK_UNABAILABLE;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.REQUEST_CANCELLED;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

/** Обработчик фильтров ссылки при ее удалении */
public class FiltersTextCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public FiltersTextCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        // запись фильтров в соответствующий объект кэша
        Optional<LinkTrackingObject> potentialTracking =
                repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.orElseThrow();
        String writingResponse = writeFilters(message.text(), tracking);
        try {
            // удаление записи из кэша
            repository.removeTrack(message.chat().id());
            // добавление ссылки в Scrapper
            service.addLinkSubscription(message.chat().id(), tracking);
            return writingResponse + LINK_REGISTERED;
        } catch (BotRequestException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionMessage().contains(message.chat().id() + " not exists")) {
                // пользователь не зарегистрирован
                return NOT_REGISTERED;
            }
            if (response.exceptionMessage().contains("unavailable")) {
                // ссылка недоступна для получения запросов
                return LINK_UNABAILABLE;
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
        return link.isPresent() && link.orElseThrow().state() == UserState.TRACKING_FILTER;
    }

    private String writeFilters(String filter, LinkTrackingObject tracking) {
        String message = FILTERS_NOT_REGISTERD;
        String[] filters = new String[0];
        if (!EMPTY_INPUT.equals(filter)) {
            filters = filter.split(" ");
            message = FILTERS_REGISTERED;
        }
        tracking.filters(filters);
        tracking.state(UserState.DEFAULT);
        return message;
    }
}
