package backend.academy.scrapper.server;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.scrapper.repository.InMemoryUserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис с бизнес-логикой добавления и удаления клиентов, ссылок, подписок в репозитории. Связывает DTO и сущности
 * репозитория
 */
@Service
@AllArgsConstructor
public class ScrapperService {

    private final InMemoryLinkRepository linkRepository;
    private final InMemorySubscriptionRepository subscrRepository;
    private final InMemoryUserRepository userRepository;

    // сервисы внешних клиентов для проверки досутпности ссылок
    private final GithubClientService githubService;
    private final StackoverflowClientService soService;

    /**
     * Метод регистрирует пользователя по id
     *
     * @param chatId id чата
     */
    public void registerUser(long chatId) {
        User user = new User(chatId);
        userRepository.registerUser(user);
    }

    /**
     * Метод удаляет пользователя из репозитория
     *
     * @param chatId id чата пользователя
     */
    public void deleteUser(long chatId) {
        User user = userRepository.getUserById(chatId);
        // удаляем подписки пользователя
        Set<Subscription> deleted = subscrRepository.deleteUserSubscriptions(user);
        // проверяем ссылки, на которые никто не подписан
        checkUnsubscribedLinks(deleted);
        // удаляем пользователя
        userRepository.deleteUserById(chatId);
    }

    /**
     * Метод возвращает подписки пользователя
     *
     * @param chatId id чата пользователя
     * @return DTO со списком ссылок
     */
    public ListLinksResponse getUserLinks(long chatId) {
        User user = userRepository.getUserById(chatId);
        Map<Long, Subscription> subscriptions = subscrRepository.getUserSubscriptions(user);
        List<LinkResponse> links = new ArrayList<>();
        // маппинг сущености подписки в DTO
        for (Map.Entry<Long, Subscription> subscription : subscriptions.entrySet()) {
            LinkResponse link = new LinkResponse(
                    subscription.getKey(),
                    subscription.getValue().link().url(),
                    subscription.getValue().tags(),
                    subscription.getValue().filters());
            links.add(link);
        }
        return new ListLinksResponse(links, links.size());
    }

    /**
     * Метод добавляет подписку пользователю
     *
     * @param chatId id пользователя
     * @param request DTO запроса на подписку
     * @return DTO ответа с зарегистрированной подпиской
     */
    public LinkResponse addSubscription(long chatId, AddLinkRequest request) {
        User user = userRepository.getUserById(chatId);
        // маппинг запроса в сущность ссылки
        // последняя проверка обновлений - текущее время
        Link link =
                new Link(request.link(), Link.getLinkType(request.link()), LocalDateTime.now(ZoneId.systemDefault()));
        // проверка доступности ссылки
        if (!isAvailable(link)) {
            throw new ScrapperUnavailableLinkException("Link is unavailable " + link);
        }
        // добавление в репозиторий
        long linkId = linkRepository.addLink(link);
        // создание новой сущности подписки и ее добавление
        Subscription newSubscription = new Subscription(chatId, user, linkId, link, request.tags(), request.filters());
        long subscriptionId = subscrRepository.addSubscription(newSubscription);
        // маппинг сущностей в DTO ответа
        return new LinkResponse(
                subscriptionId, newSubscription.link().url(), newSubscription.tags(), newSubscription.filters());
    }

    /**
     * Метод удаляет подписку пользователя
     *
     * @param chatId id чата пользователя
     * @param request DTO запроса на удаление ссылки
     * @return DTO ответа с информацией об удаленной ссылке
     */
    public LinkResponse deleteSubscription(long chatId, RemoveLinkRequest request) {
        // проверка регистрации пользователя
        if (!userRepository.containsUser(chatId)) {
            throw new ScrapperUserNotExistsException("User not exists id:" + chatId);
        }
        long linkId = linkRepository.getLinkIdByURL(request.link());
        // проверка наличия ссылки в репозитории
        if (linkId == -1) {
            throw new ScrapperLinkNotExistsException("Link with URL " + request.link() + " not found");
        }
        long subscriptionId = subscrRepository.getSubscriptionId(chatId, linkId);
        // проверка подписки пользователя на ссылку
        if (subscriptionId == -1) {
            throw new ScrapperSubscriptionNotExistsException(
                    "Subscription by user " + chatId + "on link wiht id " + linkId + " not found");
        }
        Subscription deleted = subscrRepository.removeSubscriptionById(subscriptionId);
        // проверка ссылок без подписок
        checkUnsubscribedLink(deleted);
        // маппинг сущностей в DTO
        return new LinkResponse(subscriptionId, deleted.link().url(), deleted.tags(), deleted.filters());
    }

    private void checkUnsubscribedLinks(Set<Subscription> unsubscribed) {
        for (Subscription s : unsubscribed) {
            checkUnsubscribedLink(s);
        }
    }

    /**
     * Метод проверяет, есть ли подписки в репозитории на ссылку удаленной подписки. В случае их отсутствия ссылка
     * удаляется
     *
     * @param s удаленная подписка
     */
    private void checkUnsubscribedLink(Subscription s) {
        if (subscrRepository.getLinkSubscriptions(s.link()).isEmpty()) {
            linkRepository.removeLinkById(s.linkId());
        }
    }

    /**
     * Проверка доступа к ссылке с помощью сервисов внешних клиентов
     *
     * @param link ссылка
     * @return доступна ли ссылка
     */
    private boolean isAvailable(Link link) {
        if (link.type() == LinkType.GITHUB) {
            return githubService.isLinkAvailable(link);
        } else {
            return soService.isLinkAvailable(link);
        }
    }
}
