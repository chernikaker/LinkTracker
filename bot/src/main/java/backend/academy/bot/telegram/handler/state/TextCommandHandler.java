package backend.academy.bot.telegram.handler.state;

import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.repository.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.IClient;
import com.pengrad.telegrambot.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;


public class TextCommandHandler extends CommandHandler {

    private final IClient client;

    public TextCommandHandler(InMemoryTrackingCache repository, IClient client) {
        super(repository);
        this.client = client;
    }

    @Override
    protected String processRequest(Message message) {
        Optional<LinkTrackingObject> potentialTracking = repository.getTrack(message.chat().id());
        if (potentialTracking.isEmpty()) {
            return "Неизвестная команда";
        }
        LinkTrackingObject tracking = potentialTracking.get();
        switch (tracking.state()) {
            case TRACKING_LINK:
                return writeLink(message.text(), tracking);
            case TRACKING_TAG:
                return writeTags(message.text(), tracking);
            case TRACKING_FILTER:
                String filterAddingResponse= writeFilters(message.text(), tracking);
                String answer = client.sendTrackingLink(message.chat().id(), tracking);
                repository.removeTrack(message.chat().id());
                return filterAddingResponse + " " + answer;
            default:
                throw new RuntimeException("Unknown tracking state: " + tracking.state());
        }
    }

    // TODO: validate link
    private String writeLink(String link, LinkTrackingObject tracking){
        tracking.link(link);
        tracking.state(UserState.TRACKING_TAG);
        return "Введите тэги(опционально)";
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

    private String writeFilters(String filter, LinkTrackingObject tracking) {
        if(!"-".equals(filter)){
            String[] filters = filter.split(" ");
            tracking.filters(filters);
        }
        tracking.state(UserState.DEFAULT);
        return "Фильтры установлены";
    }
}
