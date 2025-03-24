package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface DatabaseService {

    void addUser(User user);
    void addLink(Link link);

}
