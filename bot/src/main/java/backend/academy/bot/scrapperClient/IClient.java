package backend.academy.bot.scrapperClient;

import backend.academy.bot.model.LinkTrackingObject;


public interface IClient {

    String sendTrackingLink(long userId, LinkTrackingObject trackingObject);
}
