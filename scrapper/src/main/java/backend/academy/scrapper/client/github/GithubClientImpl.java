package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.dto.GithubInfo;
import backend.academy.scrapper.exception.custom.client.GithubResponseJsonIsInvalid;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClient;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class GithubClientImpl implements GithubClient {

    private final String token;
    private final String DEFAULT_URL = "https://api.github.com";
    private final RestClient githubClient;

    public GithubClientImpl(String githubToken, String baseUrl) {
        this.token = githubToken;
        this.githubClient = RestClient.builder()
            .baseUrl(baseUrl != null ? baseUrl : DEFAULT_URL)
            .build();
        System.out.println(githubToken);
    }

    public GithubClientImpl(String githubToken) {
        this(githubToken, null);
    }

    public String getCommits(String uri) {
        return githubClient.get()
            .uri(uri)
            .header("Authorization", "Bearer "+token)
            .retrieve()
            .toEntity(String.class)
            .getBody();
    }

    public String getIssues(String uri) {
        return  githubClient.get()
            .uri(uri)
            .header("Authorization", "Bearer "+token)
            .retrieve()
            .toEntity(String.class)
            .getBody();
    }

    public String getComments(String uri) {
        return   githubClient.get()
            .uri(uri)
            .header("Authorization", "Bearer "+token)
            .retrieve()
            .toEntity(String.class)
            .getBody();
    }
}
