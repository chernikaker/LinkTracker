package backend.academy.scrapper.server;

import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public ListLinksResponse getUserLinks(long chatId) {
        User user = userRepository.getUserById(chatId);
        Map<Long, Subscription> subscriptions = subscrRepository.getUserSubscriptions(user);
        List<LinkResponse> links = new ArrayList<>();
        for (Map.Entry<Long,Subscription> subscription : subscriptions.entrySet()) {
            LinkResponse link = new LinkResponse(
                subscription.getKey(),
                subscription.getValue().link().url(),
                subscription.getValue().tags(),
                subscription.getValue().filters()
            );
            links.add(link);
        }
        return new ListLinksResponse(links, links.size());
    }
}
