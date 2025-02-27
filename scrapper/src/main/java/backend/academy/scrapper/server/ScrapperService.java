package backend.academy.scrapper.server;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.custom.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.custom.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Set<Subscription> deleted = subscrRepository.deleteUserSubscriptions(user);
        checkUnsubscribedLinks(deleted);
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

    public LinkResponse addSubscription(long chatId, AddLinkRequest request) {
        User user = userRepository.getUserById(chatId);
        Link link = new Link(request.link(), Link.getLinkType(request.link()));
        long linkId = linkRepository.addLink(link);
        Subscription newSubscription = new Subscription(chatId, user, linkId, link, request.tags(), request.filters());
        long subscriptionId = subscrRepository.addSubscription(newSubscription);
        return new LinkResponse(subscriptionId, newSubscription.link().url(), newSubscription.tags(), newSubscription.filters());
    }

    public LinkResponse deleteSubscription(long chatId, RemoveLinkRequest request) {
        long linkId = linkRepository.getLinkIdByURL(request.link());
        if(linkId == -1) {
            throw new ScrapperLinkNotExistsException("Link with URL " + request.link() + " not found");
        }
        long subscriptionId = subscrRepository.getSubscriptionId(chatId, linkId);
        if(subscriptionId == -1) {
            throw new ScrapperSubscriptionNotExistsException("Subscription by user " + chatId + "on link wiht id "+linkId+" not found");
        }
        Subscription deleted = subscrRepository.removeSubscriptionById(subscriptionId);
        checkUnsubscribedLink(deleted);
        return new LinkResponse(subscriptionId, deleted.link().url(), deleted.tags(), deleted.filters());
    }

    private void checkUnsubscribedLinks(Set<Subscription> unsubscribed) {
        for (Subscription s : unsubscribed) {
           checkUnsubscribedLink(s);
        }
    }

    private void checkUnsubscribedLink(Subscription s) {
        if(subscrRepository.getLinkSubscriptions(s.link()).isEmpty()) {
            linkRepository.removeLinkById(s.linkId());
        }
    }
}
