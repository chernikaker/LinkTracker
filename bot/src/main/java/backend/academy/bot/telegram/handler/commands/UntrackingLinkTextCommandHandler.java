package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class UntrackingLinkTextCommandHandler extends CommandHandler{

    private final ScrapperClientService service;

    public UntrackingLinkTextCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    protected String processRequest(Message message) {
        repository.removeTrack(message.chat().id());
        // TODO: validation
        return service.untrackLinkSubscription(message.chat().id(), message.text());

    }

    @Override
    public boolean canHandle(Message message) {
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.get().state() == UserState.UNTRACKING_LINK;
    }
}
