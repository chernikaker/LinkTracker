package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.LINK_TRACK_MESSAGE;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.Command;
import com.pengrad.telegrambot.model.Message;

/** Обработчик команды /track */
public class TrackHandler extends CommandHandler {

    public TrackHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        // создание нового объекта для кэша и его добавление
        LinkTrackingObject newTracking = new LinkTrackingObject();
        newTracking.command(Command.TRACK);
        newTracking.state(UserState.TRACKING_LINK);
        repository.setTrack(message.chat().id(), newTracking);
        return LINK_TRACK_MESSAGE;
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если пользователь ничего не вводит
        // и команда равна /track
        return !repository.containsTrack(message.chat().id()) && message.text().equals(Command.TRACK.command());
    }
}
