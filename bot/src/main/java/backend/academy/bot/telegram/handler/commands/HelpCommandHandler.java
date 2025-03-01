package backend.academy.bot.telegram.handler.commands;

import backend.academy.bot.cache.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;

public class HelpCommandHandler extends CommandHandler{

    public HelpCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
        if(repository.containsTrack(message.chat().id())) {
            repository.removeTrack(message.chat().id());
        }
        return """
            Бот поддерживает следующие ссылки и обновления:

            GITHUB
            Ссылки вида:
            https://github.com/{владелец-репозитория}/{название-репозитория}
            * - также можно использовать http://
            Обновления:
            - commit
            - issue
            - comment

            STACKOVERFLOW
            Ссылки вида:
            https://stackoverflow.com/questions/{id-вопроса}/{название-вопроса}
            https://stackoverflow.com/questions/{id-вопроса}
            * - также можно использовать http://
            Обновления:
            - answer
            - comment
            """;
    }

    @Override
    public boolean canHandle(Message message) {
        return message.text().equals("/help");
    }
}
