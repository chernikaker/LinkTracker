package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;

/** Абстрактный класс обработчика */
public abstract class CommandHandler {

    protected final InMemoryTrackingCache repository;

    protected CommandHandler(InMemoryTrackingCache repository) {
        this.repository = repository;
    }

    public SendMessage handleMessage(Message message) {
        String responseMessage = processRequest(message);
        return new SendMessage(message.chat().id(), responseMessage);
    }

    public abstract String processRequest(Message message);

    public abstract boolean canHandle(Message message);
}
