package backend.academy.scrapper.service;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис с бизнес-логикой добавления и удаления клиентов, ссылок, подписок в репозитории. Связывает DTO и сущности
 * репозитория
 */
@Service
@AllArgsConstructor
public class ScrapperService {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final TagService tagService;
    private final FilterService filterService;

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
        userService.addUser(user);
    }

    /**
     * Метод удаляет пользователя из репозитория
     *
     * @param chatId id чата пользователя
     */
    public void deleteUser(long chatId) {
        User user = new User(chatId);
        userService.deleteUser(user);
    }

    /**
     * Метод возвращает подписки пользователя
     *
     * @param chatId id чата пользователя
     * @return DTO со списком ссылок
     */
    public ListLinksResponse getUserLinks(long chatId) {
        User u = new User(chatId);
        Map<Long, Subscription> subscriptions = subscriptionService.getUserSubscriptions(u);
        List<LinkResponse> links = new ArrayList<>();
        // маппинг сущености подписки в DTO
        for (Map.Entry<Long, Subscription> subscription : subscriptions.entrySet()) {
            LinkResponse link = new LinkResponse(
                    subscription.getKey(),
                    subscription.getValue().link().url(),
                    subscription.getValue().tags().stream().map(Tag::value).toList(),
                    subscription.getValue().filters().stream().map(f -> f.key()+":"+f.value()).toList());
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
        User user = new User(chatId);
        Link link = new Link(request.link(), LinkType.fromValue(request.link()), null);
        if (link.type() == LinkType.STACKOVERFLOW) {
            processSOLink(link);
        }
        List<Tag> tags = new ArrayList<>();
        for(String t: request.tags()) {
            tags.add(new Tag(t));
        }
        List<Filter> filters = new ArrayList<>();
        for(String f: request.filters()) {
            String[] parts = f.split(":");
            filters.add(new Filter(parts[0], parts[1]));
        }
        if(!isAvailable(link)){
            throw new ScrapperUnavailableLinkException("Link "+link.url()+" is not available");
        }
        long subscriptionId = subscriptionService.addSubscriptionOnLink(user, link, tags, filters);

        // TODO: request params?
        return new LinkResponse(
                subscriptionId, request.link(), request.tags(), request.filters());
    }

    /**
     * Метод удаляет подписку пользователя
     *
     * @param chatId id чата пользователя
     * @param request DTO запроса на удаление ссылки
     * @return DTO ответа с информацией об удаленной ссылке
     */
    public LinkResponse deleteSubscription(long chatId, RemoveLinkRequest request) {

        User user = new User(chatId);
        Link link = new Link(request.link(), LinkType.fromValue(request.link()));
        if (link.type() == LinkType.STACKOVERFLOW) {
            processSOLink(link);
        }
        Map.Entry<Long, Subscription> deletedSub = subscriptionService.deleteSubscriptionByUserAndLink(user, link);
        return new LinkResponse(
            deletedSub.getKey(),
            request.link(),
            deletedSub.getValue().tags().stream().map(Tag::value).toList(),
            deletedSub.getValue().filters().stream().map(f -> f.key()+":"+f.value()).toList()
        );
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

    private void processSOLink(Link link) {
        String url = link.url();
        if (!Character.isDigit(url.charAt(url.length() - 1))) {
            link.url(url.substring(0, url.lastIndexOf("/")));
        }
    }
}
