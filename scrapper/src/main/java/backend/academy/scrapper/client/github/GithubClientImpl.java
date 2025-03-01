package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.dto.GithubCommitInfo;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.custom.client.GithubResponseJsonIsInvalid;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
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

    public List<GithubCommitInfo> getCommits(Link link) {
        String uri = link.url().replace("https://github.com", "repos");
        System.out.println(uri);
        String jsonAns =  githubClient.get()
            .uri(uri)
            .header("Authorization", "Bearer "+token)
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
                ZoneId currentZoneId = ZoneId.systemDefault();
                ZonedDateTime timeInCurrentZone = ZonedDateTime.parse(date).withZoneSameInstant(currentZoneId);
                commits.add(new GithubCommitInfo(message, committerName, timeInCurrentZone.toLocalDateTime()));
            }
            return commits;
        } catch (JsonProcessingException e) {
            throw new GithubResponseJsonIsInvalid("Can't parse JSON response ", e);
        }
    }
}
