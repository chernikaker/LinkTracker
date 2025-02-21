package backend.academy.bot.telegram.handler.state;

import backend.academy.bot.repository.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;

public abstract class CommandHandler {

    protected final InMemoryTrackingCache repository;

    protected CommandHandler(InMemoryTrackingCache repository) {
        this.repository = repository;
    }

    public SendMessage handleMessage (Message message) {
        String responseMessage = processRequest(message);
        return new SendMessage(message.chat().id(), responseMessage);
    }

    protected abstract String processRequest(Message message);
}
