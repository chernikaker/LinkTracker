package backend.academy.scrapper.client.github;

import org.springframework.web.client.RestClient;

/** Реализация клиента Github с помощью RestClient */
public class GithubClientImpl implements GithubClient {

    private final String token;
    // дефолтный путь для API Github
    private static final String DEFAULT_URL = "https://api.github.com";
    private final RestClient githubClient;

    public GithubClientImpl(String githubToken, String baseUrl) {
        this.token = githubToken;
        this.githubClient = RestClient.builder()
                .baseUrl(baseUrl != null ? baseUrl : DEFAULT_URL)
                .build();
    }

    public GithubClientImpl(String githubToken) {
        this(githubToken, null);
    }

    @Override
    public String getResponse(String uri) {
        return githubClient
                .get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(String.class)
                .getBody();
    }
}
