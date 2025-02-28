package backend.academy.scrapper.client;

import backend.academy.dto.ListLinksResponse;
import backend.academy.scrapper.client.dto.GithubCommitInfo;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.custom.client.GithubResponseJsonIsInvalid;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
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
    }

    public GithubClientImpl(String githubToken) {
        this(githubToken, null);
    }

    public List<GithubCommitInfo> getCommits(Link link) {
        String uri = link.url().replace("https://github.com", "");

        String jsonAns =  githubClient.get()
            .uri("uri")
            .header("Authorization", token)
            .retrieve()
            .toEntity(String.class)
            .getBody();
        return parseCommits(jsonAns);
    }

    private List<GithubCommitInfo> parseCommits(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode commitsNode = objectMapper.readTree(jsonResponse);
            List<GithubCommitInfo> commits = new ArrayList<>();

            for (JsonNode commitNode : commitsNode) {
                String message = commitNode.path("commit").path("message").asText();
                String committerName = commitNode.path("commit").path("committer").path("name").asText();
                String date = commitNode.path("commit").path("committer").path("date").asText();
                commits.add(new GithubCommitInfo(message, committerName, LocalDateTime.parse(date)));
            }
            return commits;
        } catch (JsonProcessingException e) {
            throw new GithubResponseJsonIsInvalid("Can't parse JSON response ", e);
        }
    }
}
