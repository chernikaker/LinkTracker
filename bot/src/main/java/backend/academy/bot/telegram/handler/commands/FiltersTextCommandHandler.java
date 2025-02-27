package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.IClient;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;

public class FiltersTextCommandHandler extends CommandHandler {

    private final IClient client;

    public FiltersTextCommandHandler(InMemoryTrackingCache repository, IClient client) {
        super(repository);
        this.client = client;
    }

    @Override
    protected String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking = repository.getTrack(message.chat().id());
        LinkTrackingObject tracking = potentialTracking.get();
        String writingResponse = writeFilters(message.text(), tracking);
        String answer = client.sendTrackingLink(message.chat().id(), tracking);
        repository.removeTrack(message.chat().id());
        return writingResponse + "\n" + answer;
    }

    @Override
    public boolean canHandle(Message message) {
        Optional<LinkTrackingObject> link = repository.getTrack(message.chat().id());
        return link.isPresent() && link.get().state() == UserState.TRACKING_FILTER;
    }

    private String writeFilters(String filter, LinkTrackingObject tracking) {
        if(!"-".equals(filter)){
            String[] filters = filter.split(" ");
            tracking.filters(filters);
            tracking.state(UserState.DEFAULT);
            return "Фильтры установлены";
        }
        tracking.state(UserState.DEFAULT);
        return "Фильтры не установлены";
    }
}
