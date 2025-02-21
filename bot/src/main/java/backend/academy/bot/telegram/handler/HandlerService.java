package backend.academy.bot.telegram.handler;

import backend.academy.bot.model.Command;
import backend.academy.bot.telegram.handler.commands.CommandHandlerFactory;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Optional;

public class HandlerService {

    private final CommandHandlerFactory commandHandlerFactory;

    public HandlerService(
        CommandHandlerFactory commandHandlerFactory) {
        this.commandHandlerFactory = commandHandlerFactory;
    }

    public Optional<SendMessage> handle(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return Optional.empty();
        }
        Command currentCommand = Command.fromString(update.message().text());
        CommandHandler currentHandler = commandHandlerFactory.getHandlerByCommand(currentCommand);
        SendMessage result = currentHandler.handleMessage(update.message());
        return Optional.of(result);
    }
}
