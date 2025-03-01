package backend.academy.bot.scrapperClient;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

public class ScrapperRestClient implements IClient {

    private final String DEFAULT_URL = "http://localhost:8081/";
    private final RestClient restClient;

    public ScrapperRestClient(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl != null ? baseUrl : DEFAULT_URL)
                .build();
    }

    public ScrapperRestClient() {
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
                .header("Tg-Chat-Id", String.valueOf(userId))
                .retrieve()
                .toEntity(ListLinksResponse.class)
                .getBody();
    }

    @Override
    public LinkResponse addLinkSubscription(long userId, AddLinkRequest request) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(userId))
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
                .header("Tg-Chat-Id", String.valueOf(userId))
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
