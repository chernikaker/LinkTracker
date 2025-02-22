package backend.academy.bot.server;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.telegram.TelegramBotService;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BotServiceImpl implements BotService {

    private final TelegramBotService botService;

    @Override
    public void sendUpdates(LinkUpdate update) {
        for (long chat: update.tgChatIds()) {
            sendUpdateInfo(chat, update.url(), update.description());
        }
    }

    public void sendUpdateInfo(long chatId, String url, String description) {
        String messageText = "Link: " + url + " has new updates!\nDescription: " + description;
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        botService.sendResponse(sendMessage);
    }
}
