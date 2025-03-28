package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.User;

public interface UserService {

    void addUser(User user);

    void deleteUser(User user);
}
