package backend.academy.scrapper.service;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.AddLinkTagsRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkTagResponse;
import backend.academy.dto.ListLinkTagsResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.dto.RemoveLinkTagRequest;
import backend.academy.dto.RemoveTagRequest;
import backend.academy.dto.TagResponse;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
        // маппинг сущности подписки в DTO
        for (Map.Entry<Long, Subscription> subscription : subscriptions.entrySet()) {
            LinkResponse link = new LinkResponse(
                    subscription.getKey(),
                    subscription.getValue().link().url(),
                    subscription.getValue().tags().stream().map(Tag::value).toList(),
                    subscription.getValue().filters().stream()
                            .map(f -> f.key() + ":" + f.value())
                            .toList());
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
        Link link =
                new Link(request.link(), LinkType.fromValue(request.link()), LocalDateTime.now(ZoneId.systemDefault()));
        if (link.type() == LinkType.STACKOVERFLOW) {
            processSOLink(link);
        }
        if (!isAvailable(link)) {
            throw new ScrapperUnavailableLinkException("Link " + link.url() + " is not available");
        }
        List<Tag> tags = new ArrayList<>();
        for (String t : request.tags()) {
            tags.add(new Tag(t));
        }
        List<Filter> filters = new ArrayList<>();
        for (String f : request.filters()) {
            String[] parts = f.split(":");
            filters.add(new Filter(parts[0], parts[1]));
        }
        long subscriptionId = subscriptionService.addSubscriptionOnLink(user, link, tags, filters);

        return new LinkResponse(subscriptionId, request.link(), request.tags(), request.filters());
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
                deletedSub.getValue().filters().stream()
                        .map(f -> f.key() + ":" + f.value())
                        .toList());
    }

    public ListLinkTagsResponse addTagsForSubscription(long chatId, AddLinkTagsRequest request) {
        User user = new User(chatId);
        Link link = new Link(request.link(), LinkType.fromValue(request.link()));
        if (link.type() == LinkType.STACKOVERFLOW) {
            processSOLink(link);
        }
        List<Tag> tags = request.tags().stream().map(Tag::new).toList();
        Map<Long, Tag> ans = tagService.addTagsForUserAndLink(user, link, tags);
        List<TagResponse> tagResponses = new ArrayList<>();
        for (Map.Entry<Long, Tag> e : ans.entrySet()) {
            tagResponses.add(new TagResponse(e.getKey(), e.getValue().value()));
        }
        return new ListLinkTagsResponse(link.url(), new ListTagsResponse(tagResponses, tagResponses.size()));
    }

    public LinkTagResponse deleteTagForSubscription(long chatId, RemoveLinkTagRequest request) {
        User user = new User(chatId);
        Link link = new Link(request.link(), LinkType.fromValue(request.link()));
        if (link.type() == LinkType.STACKOVERFLOW) {
            processSOLink(link);
        }
        Map.Entry<Long, Tag> ans = tagService.deleteTagForSubscriptionData(user, link, request.tag());
        return new LinkTagResponse(new TagResponse(ans.getKey(), ans.getValue().value()), link.url());
    }

    public ListTagLinksResponse deleteSubscriptionsForTag(long chatId, String tagValue) {
        User user = new User(chatId);
        Tag tag = new Tag(tagValue);
        Map<Long, Subscription> ans = subscriptionService.deleteSubscriptionsByUserAndTag(user, tag);
        List<LinkResponse> linkResponses = new ArrayList<>();
        for (Map.Entry<Long, Subscription> e : ans.entrySet()) {
            linkResponses.add(mapSubscriptionToLink(e.getKey(), e.getValue()));
        }
        return new ListTagLinksResponse(tagValue, new ListLinksResponse(linkResponses, linkResponses.size()));
    }

    public ListTagLinksResponse getSubscriptionsForTag(long chatId, String tagValue) {
        User user = new User(chatId);
        Tag tag = new Tag(tagValue);
        Map<Long, Subscription> ans = subscriptionService.getSubscriptionsByUserAndTag(user, tag);
        List<LinkResponse> linkResponses = new ArrayList<>();
        for (Map.Entry<Long, Subscription> e : ans.entrySet()) {
            linkResponses.add(mapSubscriptionToLink(e.getKey(), e.getValue()));
        }
        return new ListTagLinksResponse(tagValue, new ListLinksResponse(linkResponses, linkResponses.size()));
    }

    public TagResponse deleteTag(long chatId, RemoveTagRequest request) {
        User user = new User(chatId);
        Tag tag = new Tag(request.tag());
        Map.Entry<Long, Tag> ans = tagService.deleteTagForUser(user, tag);

        return new TagResponse(ans.getKey(), ans.getValue().value());
    }

    public ListTagsResponse getTags(long chatId) {
        User user = new User(chatId);
        Map<Long, Tag> ans = tagService.getTagsForUser(user);
        List<TagResponse> tagResponses = new ArrayList<>();
        for (Map.Entry<Long, Tag> e : ans.entrySet()) {
            tagResponses.add(new TagResponse(e.getKey(), e.getValue().value()));
        }
        return new ListTagsResponse(tagResponses, tagResponses.size());
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

    private LinkResponse mapSubscriptionToLink(Long id, Subscription subscription) {
        return new LinkResponse(
                id,
                subscription.link().url(),
                subscription.tags().stream().map(Tag::value).toList(),
                subscription.filters().stream()
                        .map(f -> f.key() + ":" + f.value())
                        .toList());
    }
}
