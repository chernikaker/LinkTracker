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
    LIST("/list", "Список отслеживаемых ссылок"),
    TAGS_TO_SUB("/add_tags_to_link", "Добавить теги к существующей подписке"),
    REMOVE_TAG_SUB("/remove_tag_from_link", "Удалить тег из существующей подписки"),
    UNTRACK_BY_TAG("/untrack_by_tag", "Отписаться от всех ссылок по тегу"),
    REMOVE_TAG("/remove_tag", "Удалить тег"),
    TAGS("/tags", "Список существующих тегов"),
    LIST_BY_TAG("/list_by_tag", "Список отслеживаемых ссылок по тегу");

    private final String command;
    private final String description;
}
