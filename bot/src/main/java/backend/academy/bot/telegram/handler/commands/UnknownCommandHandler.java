package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.UNKNOWN_COMMAND_MESSAGE;

import backend.academy.bot.cache.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;

public class UnknownCommandHandler extends CommandHandler {

    public UnknownCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        return UNKNOWN_COMMAND_MESSAGE;
    }

    @Override
    public boolean canHandle(Message message) {
        return true;
    }
}
