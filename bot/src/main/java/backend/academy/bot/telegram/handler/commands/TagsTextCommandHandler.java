package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class TagsTextCommandHandler extends CommandHandler{

    public TagsTextCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking = repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.get();
        return writeTags(message.text(), tracking);
    }

    @Override
    public boolean canHandle(Message message) {
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.get().state() == UserState.TRACKING_TAG;
    }

    private String writeTags(String tagLine, LinkTrackingObject tracking) {
        if("-".equals(tagLine)){
            tracking.state(UserState.TRACKING_FILTER);
            return "Тэги не установлены. Введите фильтры(опционально)";
        }
        String[] tags = tagLine.split(" ");
        tracking.tags(tags);
        tracking.state(UserState.TRACKING_FILTER);
        return "Введите фильтры(опционально)";
    }
}
