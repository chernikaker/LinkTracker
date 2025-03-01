package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.dto.UpdateInfoType;
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

    public List<UpdateInfo> getAllInfo(Link link)
    {
        String uri = link.url().replace("https://github.com", "repos");
        List<UpdateInfo> infoList = new ArrayList<>();
        String commitData = githubClient.getCommits(uri.concat("/commits"));
        String issueData = githubClient.getIssues(uri.concat("/issues"));
        String commentData = githubClient.getComments(uri.concat("/comments"));
        infoList.addAll(parseInfo(commentData, UpdateInfoType.COMMENTS));
        infoList.addAll(parseInfo(issueData, UpdateInfoType.ISSUES));
        infoList.addAll(parseInfo(commitData, UpdateInfoType.COMMITS));
        return infoList;
    }

    private UpdateInfo parseCommit(JsonNode node) {
        String message = node.path("commit").path("message").asText();
        String committerName = node.path("author").path("login").asText();
        String date = node.path("commit").path("author").path("date").asText();
       return new UpdateInfo(message, committerName, parseDate(date), UpdateInfoType.COMMITS);
    }

    private UpdateInfo parseIssue(JsonNode node) {
        String message = node.path("title").asText();
        String authorName = node.path("user").path("login").asText();
        String date = node.path("created_at").asText();
       return new UpdateInfo(message, authorName, parseDate(date), UpdateInfoType.ISSUES);
    }

    private UpdateInfo parseComment(JsonNode node) {
        String message = node.path("body").asText();
        String authorName = node.path("user").path("login").asText();
        String date = node.path("created_at").asText();
        return new UpdateInfo(message, authorName, parseDate(date), UpdateInfoType.COMMENTS);
    }

    private LocalDateTime parseDate(String zonedDate) {
        ZoneId currentZoneId = ZoneId.systemDefault();
        ZonedDateTime timeInCurrentZone = ZonedDateTime.parse(zonedDate).withZoneSameInstant(currentZoneId);
        return timeInCurrentZone.toLocalDateTime();
    }

    private List<UpdateInfo> parseInfo(String jsonInfo, UpdateInfoType type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode commitsNode = objectMapper.readTree(jsonInfo);
            List<UpdateInfo> commits = new ArrayList<>();
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
