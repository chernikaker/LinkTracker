package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.ALREADY_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.CHAT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.REGISTRATION_CANCELLED;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;

/** Обработчик команды /start */
public class StartCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public StartCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    public String processRequest(Message message) {
        // возврат в начальное состояние из процесса ввода
        if (repository.containsTrack(message.chat().id())) {
            repository.removeTrack(message.chat().id());
        }
        try {
            // успешная регистрация
            service.registerNewClient(message.chat().id());
            return CHAT_REGISTERED;
        } catch (BotRequestException ex) {
            ApiErrorResponse response = ex.response();
            // Scrapper вернул ответ, что пользователь уже существует
            if (response.exceptionMessage().contains("already exists")) {
                return ALREADY_REGISTERED;
            } else {
                // другая ошибка обработки запроса, не зависящая от пользователя
                return REGISTRATION_CANCELLED;
            }
        }
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если оно равно команде /start
        return message.text().equals(Command.START.command());
    }
}
