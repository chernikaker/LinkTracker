package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.TAG_TRACKING_MESSAGE;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.Command;
import com.pengrad.telegrambot.model.Message;

public class RemoveTagHandler extends CommandHandler {

    public RemoveTagHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        LinkTrackingObject newTracking = new LinkTrackingObject();
        newTracking.command(Command.REMOVE_TAG);
        newTracking.state(UserState.TRACKING_TAG);
        repository.setTrack(message.chat().id(), newTracking);
        return TAG_TRACKING_MESSAGE;
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если пользователь ничего не вводит
        // и команда равна /remove_tag
        return !repository.containsTrack(message.chat().id()) && message.text().equals(Command.REMOVE_TAG.command());
    }
}
