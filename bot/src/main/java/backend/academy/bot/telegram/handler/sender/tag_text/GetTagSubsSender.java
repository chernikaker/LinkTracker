package backend.academy.bot.telegram.handler.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.FILTER_HEADER;
import static backend.academy.bot.telegram.handler.Constant.TAG_HEADER;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;
import static backend.academy.bot.telegram.handler.Constant.TAG_SUBS_EMPTY;
import static backend.academy.bot.telegram.handler.Constant.TAG_SUBS_HEADER;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListTagLinksResponse;
import org.springframework.stereotype.Component;

@Component
public class GetTagSubsSender extends TagTextSender {

    public GetTagSubsSender(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository, service);
    }

    @Override
    public String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id) {
        if (tagLine.trim().contains(" ")) {
            return TAG_INVALID_INPUT;
        }
        repository.removeTrack(id);
        try {
            ListTagLinksResponse response = service.getSubscriptionsByTag(id, tagLine.trim());
            return makeMessage(response);
        } catch (BotRequestException e) {
            return getErrorMessage(e.response(), true);
        }
    }

    private String makeMessage(ListTagLinksResponse response) {
        if (response.links().size() == 0) {
            return TAG_SUBS_EMPTY;
        }
        StringBuilder stringBuilder =
                new StringBuilder(TAG_SUBS_HEADER).append(" #").append(response.tag());
        stringBuilder.append("\n\n");
        long i = 1;
        for (LinkResponse r : response.links().links()) {
            stringBuilder.append(i).append(". ").append(r.url()).append('\n');
            stringBuilder.append(TAG_HEADER);
            for (int j = 0; j < r.tags().size(); j++) {
                stringBuilder.append("#").append(r.tags().get(j));
                if (j < r.tags().size() - 1) {
                    stringBuilder.append(' ');
                }
            }
            if (!r.filters().isEmpty()) {
                stringBuilder.append('\n');
                stringBuilder.append(FILTER_HEADER);
            }
            for (int j = 0; j < r.filters().size(); j++) {
                stringBuilder.append(r.filters().get(j));
                if (j < r.filters().size() - 1) {
                    stringBuilder.append('\n');
                }
            }
        }
        return stringBuilder.toString();
    }
}
