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
        if(message.text().startsWith("/")) {
            return false;
        }
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.get().state() == UserState.TRACKING_TAG;
    }

    private String writeTags(String tagLine, LinkTrackingObject tracking) {
        String message = "Тэги не установлены. Введите фильтры(опционально, введите '-' для пустых фильтров)";
        String[] tags = new String[0];
        if(!"-".equals(tagLine)){
            tags = tagLine.split(" ");
            message = "Тэги установлены. Введите фильтры(опционально, введите '-' для пустых фильтров)";
        }
        tracking.tags(tags);
        tracking.state(UserState.TRACKING_FILTER);
        return message;
    }
}
