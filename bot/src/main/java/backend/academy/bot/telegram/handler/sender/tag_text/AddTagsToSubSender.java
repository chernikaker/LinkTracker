package backend.academy.bot.telegram.handler.sender.tag_text;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.TAGS_ADDED_AND_SENT;

@Component
@AllArgsConstructor
public class AddTagsToSubSender implements TagTextSender {

    private final InMemoryTrackingCache repository;
    private final ScrapperClientService service;

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        String[] tags = new String[0];
        if (!EMPTY_INPUT.equals(tagLine)) {
            tags = tagLine.split(" ");
        }
        tracking.tags(tags);
        repository.removeTrack(id);
        try {
            service.addTagsToSubscription(id, tracking);
            return TAGS_ADDED_AND_SENT;
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), false);
        }
    }
}
