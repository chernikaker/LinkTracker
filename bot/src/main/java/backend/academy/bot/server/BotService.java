package backend.academy.bot.server;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.validator.LinkUpdateValidator;
import backend.academy.dto.LinkUpdate;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Сервис для обработки запросов контроллера */
@Service
@AllArgsConstructor
public class BotService {

    public static final String UPDATE_MESSAGE = "Новые уведомления для ссылки: %s %nОписание: %s";
    private final TelegramBotService botService;

    public void sendUpdates(LinkUpdate update) {
        LinkUpdateValidator.validate(update);
        for (long chat : update.tgChatIds()) {
            sendUpdateInfo(chat, update.url(), update.description());
        }
    }

    /**
     * метод оправляет сообщение с обновлением пользователям с помощью TelegramBotService.
     *
     * @param chatId чат, в который отправляется сообщение
     * @param url ссылка, по которой есть обновления
     * @param description описание обновления
     */
    public void sendUpdateInfo(long chatId, String url, String description) {
        String messageText = UPDATE_MESSAGE.formatted(url, description);
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        botService.sendResponse(sendMessage);
    }
}
