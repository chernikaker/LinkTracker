package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;

/** Абстрактный класс обработчика */
@Slf4j
public abstract class CommandHandler {

    private static final String SERVER_ERROR_MESSAGE = "Ошибка сервера";

    protected final InMemoryTrackingCache repository;

    protected CommandHandler(InMemoryTrackingCache repository) {
        this.repository = repository;
    }

    public SendMessage handleMessage(Message message) {
        try {
            String responseMessage = processRequest(message);
            return new SendMessage(message.chat().id(), responseMessage);
        } catch (RuntimeException e) {
            // перехват непредвиденных ошибок сервера, не связанных с пользователем
            log.atError().addKeyValue("message", message).setCause(e).log("Internal server error");
            return new SendMessage(message.chat().id(), SERVER_ERROR_MESSAGE);
        }
    }

    public abstract String processRequest(Message message);

    public abstract boolean canHandle(Message message);
}
