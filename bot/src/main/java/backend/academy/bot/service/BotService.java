package backend.academy.bot.service;

import static backend.academy.data.Constant.MAX_DESCRIPTION_LENGTH;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.validator.LinkUpdateValidator;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Сервис для обработки запросов контроллера */
@Service
@AllArgsConstructor
public class BotService {

    public static final String UPDATE_MESSAGE = "Новые уведомления для ссылки: %s %nОписание:%n %s";
    private final TelegramBotService telegramBotService;

    public void sendUpdates(LinkUpdate update) {
        LinkUpdateValidator.validate(update);
        for (var info : update.tgChatData().entrySet()) {
            String message = createUpdatesMessage(update.url(), update.updateUnits(), info.getValue());
            sendUpdateInfo(info.getKey(), message, update.url());
        }
    }

    /**
     * метод оправляет сообщение с обновлением пользователям с помощью TelegramBotService.
     *
     * @param chatId чат, в который отправляется сообщение
     */
    public void sendUpdateInfo(long chatId, String messageText, String link) {
        LinkPreviewOptions previewOptions = new LinkPreviewOptions()
            .url(link)
            .preferSmallMedia(false)
            .showAboveText(false);
        SendMessage sendMessage = new SendMessage(chatId, messageText).linkPreviewOptions(previewOptions);
        telegramBotService.sendResponse(sendMessage);
    }

    public String createUpdatesMessage(String url, List<LinkUpdateUnit> updates, List<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updates.size(); i++) {
            sb.append("#").append(i + 1).append('\n');
            sb.append("Тип обновления: ").append(updates.get(i).type()).append('\n');
            sb.append("Заголовок: ").append(updates.get(i).title()).append('\n');
            sb.append("Автор: ").append(updates.get(i).author()).append('\n');
            sb.append("Время обновления: ")
                    .append(updates.get(i).creationDate())
                    .append('\n');
            String descMessage = updates.get(i).description();
            if (descMessage.length() == MAX_DESCRIPTION_LENGTH) {
                descMessage += "...";
            }
            sb.append("Сообщение: ").append(descMessage).append("\n\n");
        }
        for (String tag : tags) {
            sb.append("#").append(tag).append(' ');
        }
        return UPDATE_MESSAGE.formatted(url, sb.toString());
    }
}
