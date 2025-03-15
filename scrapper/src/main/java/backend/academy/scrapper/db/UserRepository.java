package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.User;

public interface UserRepository {

    Long addUser(User user);
    User getUserByChatId(long chatId);
    void removeUserById(long userId);
}
