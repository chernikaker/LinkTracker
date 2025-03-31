package backend.academy.bot.service;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.validator.LinkUpdateValidator;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/** Сервис для обработки запросов контроллера */
@Service
@AllArgsConstructor
public class BotService {

    public static final String UPDATE_MESSAGE = "Новые уведомления для ссылки: %s %nОписание: %s";
    private final TelegramBotService telegramBotService;

    public void sendUpdates(LinkUpdate update) {
        String message = createUpdatesMessage(update.url(), update.updateUnits());
        LinkUpdateValidator.validate(update);
        for (long chat : update.tgChatIds()) {
            sendUpdateInfo(chat, update.url(), message);
        }
    }

    /**
     * метод оправляет сообщение с обновлением пользователям с помощью TelegramBotService.
     *
     * @param chatId чат, в который отправляется сообщение
     * @param url ссылка, по которой есть обновления
     */
    public void sendUpdateInfo(long chatId, String url, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        telegramBotService.sendResponse(sendMessage);
    }

    public String createUpdatesMessage(String url, List<LinkUpdateUnit> updates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updates.size(); i++) {
            sb.append("#").append(i + 1).append('\n');
            sb.append("Тип сообщения: ").append(updates.get(i).type()).append('\n');
            sb.append("Автор: ").append(updates.get(i).author()).append('\n');
            sb.append("Время обновления: ").append(updates.get(i).creationDate()).append('\n');
            sb.append("Описание: ").append(updates.get(i).description()).append('\n');
        }
        return UPDATE_MESSAGE.formatted(url, sb.toString());
    }
}
