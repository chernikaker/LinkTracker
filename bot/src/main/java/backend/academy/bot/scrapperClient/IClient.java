package backend.academy.bot.scrapperClient;

import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.ListLinksResponse;


public interface IClient {

    String sendTrackingLink(long userId, LinkTrackingObject trackingObject);

    void registerChat(long userId);

    ListLinksResponse getUserLinks(long userId);
}
