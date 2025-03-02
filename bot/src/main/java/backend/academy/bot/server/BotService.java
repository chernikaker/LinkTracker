package backend.academy.bot.server;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.validator.LinkUpdateValidator;
import backend.academy.dto.LinkUpdate;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BotService {

    private final TelegramBotService botService;
    private final LinkUpdateValidator validator;

    public void sendUpdates(LinkUpdate update) {
        validator.validate(update);
        for (long chat : update.tgChatIds()) {
            sendUpdateInfo(chat, update.url(), update.description());
        }
    }

    public void sendUpdateInfo(long chatId, String url, String description) {
        String messageText = "Новые уведомления для ссылки: " + url + " \nОписание: " + description;
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        botService.sendResponse(sendMessage);
    }
}
