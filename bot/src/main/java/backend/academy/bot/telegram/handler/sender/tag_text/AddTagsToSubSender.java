package backend.academy.bot.telegram.handler.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.TAGS_ADDED_AND_SENT;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import org.springframework.stereotype.Component;

@Component
public class AddTagsToSubSender extends TagTextSender {

    public AddTagsToSubSender(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository, service);
    }

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        tracking.tags(tagLine.split(" "));
        repository.removeTrack(id);
        try {
            service.addTagsToSubscription(id, tracking);
            return TAGS_ADDED_AND_SENT;
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), false);
        }
    }
}
