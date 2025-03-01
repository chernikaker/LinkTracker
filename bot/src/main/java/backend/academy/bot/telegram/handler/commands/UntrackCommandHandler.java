package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import com.pengrad.telegrambot.model.Message;

public class UntrackCommandHandler extends CommandHandler {

    public UntrackCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
        LinkTrackingObject newTracking = new LinkTrackingObject();
        newTracking.state(UserState.UNTRACKING_LINK);
        repository.setTrack(message.chat().id(), newTracking);
        return "Введите ссылку для удаления";
    }

    @Override
    public boolean canHandle(Message message) {
        return !repository.containsTrack(message.chat().id()) && message.text().equals("/untrack");
    }
}
