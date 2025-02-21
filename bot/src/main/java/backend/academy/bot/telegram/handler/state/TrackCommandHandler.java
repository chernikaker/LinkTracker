package backend.academy.bot.telegram.handler.state;

import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.repository.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class TrackCommandHandler extends CommandHandler {

    public TrackCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking = repository.getTrack(message.chat().id());
        if(potentialTracking.isPresent()) {
            return "Эта команда сейчас недоступна";
        }
        LinkTrackingObject newTracking = new LinkTrackingObject();
        repository.setTrack(message.chat().id(), newTracking);
        return "Введите ссылку для отслеживания";
    }
}
