package backend.academy.bot.telegram.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Класс с перечислением команд бота и их описаний */
@AllArgsConstructor
@Getter
public enum Command {
    START("/start", "Запуск бота и регистрация пользователя"),
    HELP("/help", "Помощь"),
    UNTRACK("/untrack", "Прекратить отслеживание ссылки"),
    TRACK("/track", "Добавить ссылку для отслеживания"),
    LIST("/list", "Список отслеживаемых ссылок");

    private final String command;
    private final String description;
}
