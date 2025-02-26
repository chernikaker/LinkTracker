package backend.academy.scrapper.server;

import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ScrapperService {

    private final InMemoryLinkRepository linkRepository;
    private final InMemorySubscriptionRepository subscrRepository;
    private final InMemoryUserRepository userRepository;

    public void registerUser(long chatId) {
        User user = new User(chatId);
        userRepository.registerUser(user);
    }

    public void deleteUser(long chatId) {
        User user = userRepository.getUserById(chatId);
        subscrRepository.deleteUserSubscriptions(user);
        userRepository.deleteUserById(chatId);
    }


}
