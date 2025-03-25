package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.User;

public interface UserService {

    void addUser(User user);

    void deleteUser(User user);
}
