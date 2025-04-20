package backend.academy.bot.telegram.handler.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.TAG_DETACHED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import org.springframework.stereotype.Component;

@Component
public class RemoveTagFromSubSender extends TagTextSender {

    public RemoveTagFromSubSender(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository, service);
    }

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        if (tagLine.trim().contains(" ")) {
            return TAG_INVALID_INPUT;
        }
        tracking.tags(new String[] {tagLine.trim()});
        repository.removeTrack(id);
        try {
            service.removeTagForSubscription(id, tracking);
            return TAG_DETACHED;
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), false);
        }
    }
}
