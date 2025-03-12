package backend.academy.bot.scrapperClient;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
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
}
