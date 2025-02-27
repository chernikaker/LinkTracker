package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.IClient;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import com.pengrad.telegrambot.model.Message;

public class ListCommandHandler extends CommandHandler {

    private final ScrapperClientService service;

    public ListCommandHandler(InMemoryTrackingCache repository, ScrapperClientService service) {
        super(repository);
        this.service = service;
    }

    @Override
    protected String processRequest(Message message) {
        return service.getUserLinks(message.chat().id());
    }

    @Override
    public boolean canHandle(Message message) {
        return message.text().equals("/list");
    }
}
