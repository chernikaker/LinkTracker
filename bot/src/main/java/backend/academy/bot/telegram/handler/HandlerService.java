package backend.academy.bot.telegram.handler;

import backend.academy.bot.telegram.handler.commands.CommandHandler;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HandlerService {

    public HandlerService(List<CommandHandler> commandHandlers) {
        this.handlers = commandHandlers;
    }

    private final List<CommandHandler> handlers;

    public Optional<SendMessage> handle(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return Optional.empty();
        }
        CommandHandler currentHandler = getHandlerByMessage(update.message());
        SendMessage result = currentHandler.handleMessage(update.message());
        return Optional.of(result);
    }

    public CommandHandler getHandlerByMessage(Message message) {
        for (CommandHandler handler : handlers) {
            if (handler.canHandle(message)) {
                return handler;
            }
        }
        return handlers.getLast();
    }
}
