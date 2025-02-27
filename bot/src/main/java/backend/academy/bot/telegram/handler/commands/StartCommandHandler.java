package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import com.pengrad.telegrambot.model.Message;

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
       return service.registerNewClient(message.chat().id());
    }


    @Override
    public boolean canHandle(Message message) {
        return message.text().equals("/start");
    }
}
