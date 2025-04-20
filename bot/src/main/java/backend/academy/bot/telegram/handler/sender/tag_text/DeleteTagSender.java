package backend.academy.bot.telegram.handler.sender.tag_text;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import static backend.academy.bot.telegram.handler.Constant.TAG_DELETED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;

@Component
public class DeleteTagSender extends TagTextSender {

    public DeleteTagSender(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository, service);
    }

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        if(tagLine.trim().contains(" ")) {
            return TAG_INVALID_INPUT;
        }
        repository.removeTrack(id);
        try {
            service.deleteTag(id, tagLine.trim());
            return TAG_DELETED;
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), true);
        }
    }
}
