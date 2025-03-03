package backend.academy.scrapper.client.bot;

import backend.academy.dto.LinkUpdate;

/** Контракт клиента Bot, соответствующий OpenAPI */
public interface BotClient {

    void sendUpdates(LinkUpdate update);
}
