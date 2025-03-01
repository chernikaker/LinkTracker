package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class LinkTextCommandHandler extends CommandHandler{

    public LinkTextCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking = repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.get();
        return writeLink(message.text(), tracking);
    }

    @Override
    public boolean canHandle(Message message) {
        if(message.text().startsWith("/")) {
            return false;
        }
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.get().state() == UserState.TRACKING_LINK;
    }

    // TODO: validate link
    private String writeLink(String link, LinkTrackingObject tracking){
        tracking.link(link);
        tracking.state(UserState.TRACKING_TAG);
        return "Введите тэги(опционально)";
    }
}
