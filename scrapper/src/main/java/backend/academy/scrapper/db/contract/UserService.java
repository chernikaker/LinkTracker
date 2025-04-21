package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.User;

/** Контракт сервиса, управляющего пользователями в БД */
public interface UserService {

    /** добавление пользователя */
    void addUser(User user);

    /** удаление пользователя */
    void deleteUser(User user);
}
