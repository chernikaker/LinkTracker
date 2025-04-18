package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.ENTER_FILTER;
import static backend.academy.bot.telegram.handler.Constant.ENTER_FILTER_NO_TAGS;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.bot.telegram.handler.sender.tag_text.TagCommandSenderFactory;
import backend.academy.bot.telegram.handler.sender.tag_text.TagTextSender;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

/** Обработчик тегов ссылки при ее удалении */
public class TagsTextHandler extends CommandHandler {

    private final TagCommandSenderFactory factory;

    public TagsTextHandler(InMemoryTrackingCache repository, TagCommandSenderFactory factory) {
        super(repository);
        this.factory = factory;
    }

    @Override
    public String processRequest(Message message) {
        // записывает тэги в соответствующий объект кэша
        Optional<LinkTrackingObject> potentialTracking =
                repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.orElseThrow();
        if(tracking.command() == Command.TRACK) {
            return writeTags(message.text(), tracking);
        } else {
            TagTextSender sender = factory.getSenderByCommand(tracking.command());
            return sender.writeTagAndSendRequest(message.text(), tracking, message.chat().id());
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
