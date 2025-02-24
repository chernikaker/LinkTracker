package backend.academy.bot.server;

import backend.academy.dto.LinkUpdate;

public interface BotService {
    void sendUpdates(LinkUpdate update);
}
