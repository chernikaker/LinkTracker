package backend.academy.bot.model;

/** Класс описывает все возможные состояния ввода пользователя при вводе ссылки на добавление/удаление */
public enum UserState {
    DEFAULT,
    TRACKING_LINK,
    TRACKING_TAG,
    TRACKING_FILTER,
    UNTRACKING_LINK;
}
