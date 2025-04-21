package backend.academy.bot.telegram.handler.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.SUBS_DELETED_BY_TAG;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import org.springframework.stereotype.Component;

/**
 * Класс обработки и посылки сообщения об удалении подписок по тегу
 */
@Component
public class UntrackByTagSender extends TagTextSender {

    public UntrackByTagSender(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository, service);
    }

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        // тег может быть только один
        if (tagLine.trim().contains(" ")) {
            return TAG_INVALID_INPUT;
        }
        repository.removeTrack(id);
        try {
            service.removeSubscriptionsByTag(id, tagLine.trim());
            return SUBS_DELETED_BY_TAG;
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), true);
        }
    }
}
