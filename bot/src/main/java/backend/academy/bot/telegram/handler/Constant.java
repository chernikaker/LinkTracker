package backend.academy.bot.telegram.handler;

import lombok.experimental.UtilityClass;

/** Класс констант с сообщениями для пользователя */
@UtilityClass
public class Constant {
    // common
    public static final String EMPTY_INPUT = "-";
    public static final String NOT_REGISTERED = "Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start";
    public static final String REQUEST_CANCELLED = "Запрос отклонен, попробуйте ещё раз";
    public static final String LINK_NOT_VALID =
            """
        Ссылка введена неверно или не поддерживается, попробуйте ещё раз.
        Подробнее о формате в /help
        """;

    // FiltersTextCommandHandler
    public static final String LINK_REGISTERED = "\nСсылка успешно зарегистрирована!";
    public static final String LINK_UNABAILABLE = "Введенная ссылка недоступна, запрос отклонен";
    public static final String FILTERS_NOT_REGISTERD = "Фильтры не установлены";
    public static final String FILTERS_REGISTERED = "Фильтры установлены";

    // HelpCommandHandler
    public static final String HELP_MESSAGE =
            """
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

    // LinkTextCommandHandler
    public static final String TAGS_TRACKING_MESSAGE = "Введите тэги(опционально, введите '-' для пустых тегов)";

    // ListCommandHandler
    public static final String NO_LINKS = "Отслеживаемых ссылок нет";
    public static final String LINK_HEADER = "Отслеживаемые ссылки:\n\n";
    public static final String TAG_HEADER = "Теги:\n";
    public static final String FILTER_HEADER = "Фильтры:\n";

    // StartCommandHandler
    public static final String CHAT_REGISTERED = "Чат успешно зарегистрирован";
    public static final String ALREADY_REGISTERED = "Вы уже зарегистрированы";
    public static final String REGISTRATION_CANCELLED = "Регистрация отклонена, попробуйте ещё раз";

    // TagsTextCommandHandler
    public static final String ENTER_FILTER_NO_TAGS =
            "Тэги не установлены. Введите фильтры(опционально, введите %s для пустых фильтров)";
    public static final String ENTER_FILTER =
            "Тэги установлены. Введите фильтры(опционально, введите %s для пустых фильтров)";

    // TrackCommandHandler
    public static final String LINK_TRACK_MESSAGE = "Введите ссылку для отслеживания";

    // UnknownCommandHandler
    public static final String UNKNOWN_COMMAND_MESSAGE = "Команда неизвестна или недоступна на данный момент";

    // UntrackCommandHandler
    public static final String LINK_UNTRACK_TEXT = "Введите ссылку для удаления";

    // UntrackingLinkTextCommandHandler
    public static final String LINK_REMOVED_SUCCESS = "Ссылка успешно удалена";
    public static final String NO_SUBSCRIPTION = "У вас нет подписки на данную ссылку";
}
