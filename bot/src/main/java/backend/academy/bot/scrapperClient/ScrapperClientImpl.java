package backend.academy.bot.scrapperClient;

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
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

/** Реализация клиента с помощью RestClient. Присутствует возможность указать базовый URL или использовать дефолтный. */
public class ScrapperClientImpl implements ScrapperClient {

    private static final String DEFAULT_URL = "http://localhost:8081/";
    private static final String TG_ID_HEADER = "Tg-Chat-Id";
    private final RestClient restClient;

    public ScrapperClientImpl(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl != null ? baseUrl : DEFAULT_URL)
                .build();
    }

    public ScrapperClientImpl() {
        this(null);
    }

    @Override
    public void registerChat(long userId) {
        restClient.post().uri("tg-chat/{id}", userId).retrieve().toBodilessEntity();
    }

    @Override
    public ListLinksResponse getUserLinks(long userId) {
        return restClient
                .get()
                .uri("/links")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .retrieve()
                .toEntity(ListLinksResponse.class)
                .getBody();
    }

    @Override
    public LinkResponse addLinkSubscription(long userId, AddLinkRequest request) {
        return restClient
                .post()
                .uri("/links")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .body(request)
                .retrieve()
                .toEntity(LinkResponse.class)
                .getBody();
    }

    @Override
    public void deleteLinkSubscription(long userId, RemoveLinkRequest request) {
        restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public ListLinkTagsResponse addTagsToSubscription(long userId, AddLinkTagsRequest request) {
        return restClient
                .post()
                .uri("/links/tags")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .body(request)
                .retrieve()
                .toEntity(ListLinkTagsResponse.class)
                .getBody();
    }

    @Override
    public LinkTagResponse removeTagFromSubscription(long userId, RemoveLinkTagRequest request) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links/tags")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .body(request)
                .retrieve()
                .toEntity(LinkTagResponse.class)
                .getBody();
    }

    @Override
    public ListTagLinksResponse removeSubscriptionsByTag(long userId, String tag) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/tags/" + tag + "/links")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .retrieve()
                .toEntity(ListTagLinksResponse.class)
                .getBody();
    }

    @Override
    public TagResponse deleteTag(long userId, RemoveTagRequest request) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/tags")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .body(request)
                .retrieve()
                .toEntity(TagResponse.class)
                .getBody();
    }

    @Override
    public ListTagLinksResponse getTagSubscriptions(long userId, String tag) {
        return restClient
                .get()
                .uri("/tags/" + tag + "/links")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .retrieve()
                .toEntity(ListTagLinksResponse.class)
                .getBody();
    }

    @Override
    public ListTagsResponse getTags(long userId) {
        return restClient
                .get()
                .uri("/tags")
                .header(TG_ID_HEADER, String.valueOf(userId))
                .retrieve()
                .toEntity(ListTagsResponse.class)
                .getBody();
    }
}
