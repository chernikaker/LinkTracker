package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.LINK_NOT_VALID;
import static backend.academy.bot.telegram.handler.Constant.TAGS_TRACKING_MESSAGE;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.validator.LinkUrlValidator;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

/** Обработчик текста ссылки при ее добавлении */
public class LinkTextCommandHandler extends CommandHandler {

    public LinkTextCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        // записывает ссылку в соответствующий объект кэша
        Optional<LinkTrackingObject> potentialTracking =
                repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.orElseThrow();
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
        // валидация по формату ссылки
        if (LinkUrlValidator.isValid(link)) {
            tracking.link(link);
            tracking.state(UserState.TRACKING_TAG);
            return TAGS_TRACKING_MESSAGE.formatted(EMPTY_INPUT);
        } else {
            return LINK_NOT_VALID;
        }
    }
}
