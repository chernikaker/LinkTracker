package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import com.pengrad.telegrambot.model.Message;

public class TrackCommandHandler extends CommandHandler {

    public TrackCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        LinkTrackingObject newTracking = new LinkTrackingObject();
        repository.setTrack(message.chat().id(), newTracking);
        return "Введите ссылку для отслеживания";
    }

    @Override
    public boolean canHandle(Message message) {
        return !repository.containsTrack(message.chat().id()) && message.text().startsWith("/track");
    }
}
