package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUserRepository {

    private final Map<Long, User> users = new HashMap<>();

    public long registerUser(User user) {
        long id = user.chatId();
        if (users.containsKey(id)) {
            throw new ScrapperUserAlreadyExistsException("User " + id + " already exists");
        }
        users.put(id, user);
        return id;
    }

    public User getUserById(long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new ScrapperUserNotExistsException("User " + userId + " does not exist");
        }
        return user;
    }

    public void deleteUserById(long userId) {
        if (users.remove(userId) == null) {
            throw new ScrapperUserNotExistsException("User " + userId + " does not exist");
        }
    }

    public void clear() {
        users.clear();
    }
}
