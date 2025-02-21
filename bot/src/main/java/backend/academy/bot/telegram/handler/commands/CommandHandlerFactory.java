package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.model.Command;
import java.util.Map;


public class CommandHandlerFactory {

    private final Map<Command, CommandHandler> handlers;

    public CommandHandlerFactory(Map<Command, CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public CommandHandler getHandlerByCommand(Command command) {
        return handlers.get(command);

    }
}
