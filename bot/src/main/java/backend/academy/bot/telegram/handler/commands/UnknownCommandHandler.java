package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;

public class UnknownCommandHandler extends CommandHandler{

    public UnknownCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
        return "Неизвестная команда";
    }

    @Override
    public boolean canHandle(Message message) {
        return true;
    }
}
