package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.ENTER_FILTER;
import static backend.academy.bot.telegram.handler.Constant.ENTER_FILTER_NO_TAGS;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

/** Обработчик тегов ссылки при ее удалении */
public class TagsTextCommandHandler extends CommandHandler {

    public TagsTextCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        // записывает тэги в соответствующий объект кэша
        Optional<LinkTrackingObject> potentialTracking =
                repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.orElseThrow();
        return writeTags(message.text(), tracking);
    }

    @Override
    public boolean canHandle(Message message) {
        // не поддерживает никакие команды
        if (message.text().startsWith("/")) {
            return false;
        }
        // проверка, что запись есть в кэше и состояние пользователя соответствующее
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.orElseThrow().state() == UserState.TRACKING_TAG;
    }

    private String writeTags(String tagLine, LinkTrackingObject tracking) {
        String message = ENTER_FILTER_NO_TAGS.formatted(EMPTY_INPUT);
        String[] tags = new String[0];
        if (!EMPTY_INPUT.equals(tagLine)) {
            tags = tagLine.split(" ");
            message = ENTER_FILTER.formatted(EMPTY_INPUT);
        }
        tracking.tags(tags);
        tracking.state(UserState.TRACKING_FILTER);
        return message;
    }
}
