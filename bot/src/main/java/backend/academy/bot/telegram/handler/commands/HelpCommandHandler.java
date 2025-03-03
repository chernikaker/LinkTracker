package backend.academy.bot.telegram.handler.commands;

import static backend.academy.bot.telegram.handler.Constant.HELP_MESSAGE;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.telegram.handler.Command;
import com.pengrad.telegrambot.model.Message;

/** Обработчик команды /help */
public class HelpCommandHandler extends CommandHandler {

    public HelpCommandHandler(InMemoryTrackingCache repository) {
        super(repository);
    }

    @Override
    public String processRequest(Message message) {
        // возврат в начальное состояние из процесса ввода
        if (repository.containsTrack(message.chat().id())) {
            repository.removeTrack(message.chat().id());
        }
        return HELP_MESSAGE;
    }

    @Override
    public boolean canHandle(Message message) {
        // может обработать сообщение, если оно равно команде /help
        return message.text().equals(Command.HELP.command());
    }
}
