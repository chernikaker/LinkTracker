package backend.academy.bot.telegram.handler;

import backend.academy.bot.config.HandlersConfig;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для выбора обработчика сообщения и обработки обновления
 *
 * @see HandlersConfig
 */
@Slf4j
@Service
@AllArgsConstructor
public class HandlerService {

    private final List<CommandHandler> handlers;

    public Optional<SendMessage> handle(Update update) {
        // проверка наличия текста сообщения в обновлении
        if (update.message() == null || update.message().text() == null) {
            return Optional.empty();
        }
        // выбор обработчика и получение ответа
        CommandHandler currentHandler = getHandlerByMessage(update.message());
        SendMessage result = currentHandler.handleMessage(update.message())
            .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));;
        return Optional.of(result);
    }

    public CommandHandler getHandlerByMessage(Message message) {
        // перебор списка обработчиков и выбор первого доступного
        // подробнее о формировании списка в HandlersConfig
        for (CommandHandler handler : handlers) {
            if (handler.canHandle(message)) {
                return handler;
            }
        }
        log.atWarn().addKeyValue("message", message).log("Can't find handler for message, used last");
        return handlers.getLast();
    }
}
