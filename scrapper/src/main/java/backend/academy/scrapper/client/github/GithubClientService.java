package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.github.dto.GithubInfo;
import backend.academy.scrapper.client.github.dto.GithubInfoType;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.custom.client.GithubResponseJsonIsInvalid;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class GithubClientService {

    private final GithubClient githubClient;

    public List<GithubInfo> getAllInfo(Link link)
    {
        String uri = link.url().replace("https://github.com", "repos");
        List<GithubInfo> infoList = new ArrayList<>();
        String commitData = githubClient.getCommits(uri.concat("/commits"));
        String issueData = githubClient.getIssues(uri.concat("/issues"));
        String commentData = githubClient.getComments(uri.concat("/comments"));
        infoList.addAll(parseInfo(commentData, GithubInfoType.COMMENTS));
        infoList.addAll(parseInfo(issueData, GithubInfoType.ISSUES));
        infoList.addAll(parseInfo(commitData, GithubInfoType.COMMITS));
        return infoList;
    }

    private GithubInfo parseCommit(JsonNode node) {
        String message = node.path("commit").path("message").asText();
        String committerName = node.path("author").path("login").asText();
        String date = node.path("commit").path("author").path("date").asText();
       return new GithubInfo(message, committerName, parseDate(date), GithubInfoType.COMMITS);
    }

    private GithubInfo parseIssue(JsonNode node) {
        String message = node.path("title").asText();
        String authorName = node.path("user").path("login").asText();
        String date = node.path("created_at").asText();
       return new GithubInfo(message, authorName, parseDate(date), GithubInfoType.ISSUES);
    }

    private GithubInfo parseComment(JsonNode node) {
        String message = node.path("body").asText();
        String authorName = node.path("user").path("login").asText();
        String date = node.path("created_at").asText();
        return new GithubInfo(message, authorName, parseDate(date), GithubInfoType.COMMENTS);
    }

    private LocalDateTime parseDate(String zonedDate) {
        ZoneId currentZoneId = ZoneId.systemDefault();
        ZonedDateTime timeInCurrentZone = ZonedDateTime.parse(zonedDate).withZoneSameInstant(currentZoneId);
        return timeInCurrentZone.toLocalDateTime();
    }

    private List<GithubInfo> parseInfo(String jsonInfo, GithubInfoType type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode commitsNode = objectMapper.readTree(jsonInfo);
            List<GithubInfo> commits = new ArrayList<>();
            for (JsonNode commitNode : commitsNode) {
                switch (type) {
                    case COMMITS:
                        commits.add(parseCommit(commitNode));
                        break;
                    case ISSUES:
                        commits.add(parseIssue(commitNode));
                        break;
                    case COMMENTS:
                        commits.add(parseComment(commitNode));
                        break;
                }
            }
            return commits;
        } catch (JsonProcessingException e) {
            throw new GithubResponseJsonIsInvalid("Can't parse JSON response ", e);
        }
    }
}
