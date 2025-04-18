package backend.academy.bot.telegram.handler.sender.tag_text;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import static backend.academy.bot.telegram.handler.Constant.TAG_DETACHED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;

@Component
@AllArgsConstructor
public class RemoveTagFromSubSender implements TagTextSender {

    private final InMemoryTrackingCache repository;
    private final ScrapperClientService service;

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        if(tagLine.trim().contains(" ")) {
            return TAG_INVALID_INPUT;
        }
        tracking.tags(new String[]{tagLine.trim()});
        repository.removeTrack(id);
        try {
            service.removeTagBySubscription(id, tracking);
            return TAG_DETACHED;
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), false);
        }
    }
}
