package backend.academy.scrapper.client.bot;

import backend.academy.dto.LinkUpdate;

public interface BotClient {

    void sendUpdates(LinkUpdate update);
}
