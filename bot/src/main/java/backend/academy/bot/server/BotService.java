package backend.academy.bot.server;

import backend.academy.bot.dto.LinkUpdate;

public interface BotService {
    void sendUpdates(LinkUpdate update);
}
