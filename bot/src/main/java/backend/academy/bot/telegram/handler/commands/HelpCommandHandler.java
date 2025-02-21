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
            return "Команда сейчас недоступна. Для прекращения добавления ссылки выполните /start";
        }
        return """
            Основные команды
            1) /start - регистрация в боте и сброс текущих команд
            2) /track - добавление ссылки
               параметры: тэги (опционально), фильтры(опционально)
            3) /untrack - прекратить отслеживание ссылки
            4) /list - показать список отслеживаемых ссылок
            5) /help - помощь
            """;
    }
}
