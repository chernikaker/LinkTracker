package backend.academy.bot.scrapperClient;

import backend.academy.bot.exception.custom.scrapperClient.BotChatRegistrationException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidChatIdException;
import backend.academy.bot.exception.custom.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ScrapperRestClient implements IClient {

    private final RestClient restClient;

    public ScrapperRestClient(String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public void registerChat(long userId) {
        try {
            restClient.post()
                .uri("tg-chat/{id}", userId)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            throw new BotChatRegistrationException(error);
        }
    }

    @Override
    public ListLinksResponse getUserLinks(long userId) {
        try {
            return restClient.get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(userId))
                .retrieve()
                .toEntity(ListLinksResponse.class)
                .getBody();
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            throw new BotInvalidChatIdException(error);
        }
    }

    @Override
    public LinkResponse addLinkSubscription(long userId, AddLinkRequest request) {
        try{
            return restClient.post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(userId))
                .body(request)
                .retrieve()
                .toEntity(LinkResponse.class)
                .getBody();
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            throw new BotInvalidLinkRequestException(error);
        }
    }

    @Override
    public void deleteLinkSubscription(long userId, RemoveLinkRequest request) {
        try {
            restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(userId))
                .body(request)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            throw new BotInvalidLinkRequestException(error);
        }
    }
}
