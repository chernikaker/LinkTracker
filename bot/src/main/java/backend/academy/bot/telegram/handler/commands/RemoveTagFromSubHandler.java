package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.LINK_TRACK_MESSAGE;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.Command;
import com.pengrad.telegrambot.model.Message;

/**
 * Обработчик команды открепления тега от ссылки
 */
public class RemoveTagFromSubHandler extends CommandHandler {

    public RemoveTagFromSubHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        // создание нового объекта для кэша
        LinkTrackingObject newTracking = new LinkTrackingObject();
        newTracking.command(Command.REMOVE_TAG_SUB);
        // следующее состояние - ввод ссылки
        newTracking.state(UserState.TRACKING_LINK);
        repository.setTrack(message.chat().id(), newTracking);
        return LINK_TRACK_MESSAGE;
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если пользователь ничего не вводит
        // и команда равна /remove_tag_from_link
        return !repository.containsTrack(message.chat().id())
                && message.text().equals(Command.REMOVE_TAG_SUB.command());
    }
}
