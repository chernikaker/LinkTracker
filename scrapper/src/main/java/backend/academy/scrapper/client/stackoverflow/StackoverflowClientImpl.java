package backend.academy.scrapper.client.stackoverflow;

import org.springframework.web.client.RestClient;

public class StackoverflowClientImpl implements StackoverflowClient {

    private final String token;
    private final String apiKey;
    private final String DEFAULT_URL = "https://api.stackexchange.com/2.3";
    private final RestClient restClient;

    public StackoverflowClientImpl(String accessToken, String apiKey, String baseUrl) {
        this.token = accessToken;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl != null ? baseUrl : DEFAULT_URL)
            .build();
    }

    public StackoverflowClientImpl(String accessToken, String apiKey) {
        this(accessToken, apiKey, null);
    }

    @Override
    public String getResponse(String uri) {
        return   restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(uri)
                .queryParam("key", apiKey)
                .queryParam("site", "stackoverflow")
                .build())
            .header("Authorization", "Bearer "+token)
            .retrieve()
            .toEntity(String.class)
            .getBody();
    }
}
