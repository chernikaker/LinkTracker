package backend.academy.bot.telegram.handler.state;

import backend.academy.bot.repository.InMemoryTrackingCache;
import com.pengrad.telegrambot.model.Message;

public class StartCommandHandler extends CommandHandler {

    public StartCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    protected String processRequest(Message message) {
       if (setUserToDB(message.chat().id())) {
           return "Вы зарегистрированы!";
       } else {
           if(repository.containsTrack(message.chat().id())) {
               repository.removeTrack(message.chat().id());
           }
           return "Вы уже зарегистрированы. Текущие команды сброшены";
       }
    }

    private boolean setUserToDB(long id) {
        //TODO: add method
        return false;
    }
}
