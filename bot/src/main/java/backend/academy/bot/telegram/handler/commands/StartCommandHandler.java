package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotChatRegistrationException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Component;

public class StartCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public StartCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    protected String processRequest(Message message) {
       if (repository.containsTrack(message.chat().id())) {
           repository.removeTrack(message.chat().id());
       }
        try {
            service.registerNewClient(message.chat().id());
            return "Чат успешно зарегистрирован";
        } catch (BotChatRegistrationException ex) {
            ApiErrorResponse response = ex.response();
            if (response.exceptionMessage().contains("already exists")) {
                return "Вы уже зарегистрированы";
            } else {
                return "Регистрация отклонена, попробуйте ещё раз";
            }
        }
    }

    @Override
    public boolean canHandle(Message message) {
        return message.text().equals("/start");
    }
}
