package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.LINK_UNTRACK_TEXT;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.Command;
import com.pengrad.telegrambot.model.Message;

/** Обработчик команды /untrack */
public class UntrackHandler extends CommandHandler {

    public UntrackHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        // создание нового объекта для кэша и его добавление
        LinkTrackingObject newTracking = new LinkTrackingObject();
        newTracking.command(Command.UNTRACK);
        newTracking.state(UserState.TRACKING_LINK);
        repository.setTrack(message.chat().id(), newTracking);
        return LINK_UNTRACK_TEXT;
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если пользователь ничего не вводит
        // и команда равна /untrack
        return !repository.containsTrack(message.chat().id()) && message.text().equals(Command.UNTRACK.command());
    }
}
