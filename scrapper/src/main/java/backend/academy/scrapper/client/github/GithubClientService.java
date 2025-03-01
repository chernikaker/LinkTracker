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
        String commitData = githubClient.getResponse(uri.concat("/commits"));
        String issueData = githubClient.getResponse(uri.concat("/issues"));
        String commentData = githubClient.getResponse(uri.concat("/comments"));
        infoList.addAll(parseInfo(commentData, UpdateInfoType.COMMENT));
        infoList.addAll(parseInfo(issueData, UpdateInfoType.ISSUE));
        infoList.addAll(parseInfo(commitData, UpdateInfoType.COMMIT));
        return infoList;
    }

    private UpdateInfo parseCommit(JsonNode node) {
        String message = node.path("commit").path("message").asText();
        String committerName = node.path("author").path("login").asText();
        String date = node.path("commit").path("author").path("date").asText();
       return new UpdateInfo(message, committerName, parseDate(date), UpdateInfoType.COMMIT);
    }

    private UpdateInfo parseIssue(JsonNode node) {
        String message = node.path("title").asText();
        String authorName = node.path("user").path("login").asText();
        String date = node.path("created_at").asText();
       return new UpdateInfo(message, authorName, parseDate(date), UpdateInfoType.ISSUE);
    }

    private UpdateInfo parseComment(JsonNode node) {
        String message = node.path("body").asText();
        String authorName = node.path("user").path("login").asText();
        String date = node.path("created_at").asText();
        return new UpdateInfo(message, authorName, parseDate(date), UpdateInfoType.COMMENT);
    }

    private LocalDateTime parseDate(String zonedDate) {
        ZoneId currentZoneId = ZoneId.systemDefault();
        ZonedDateTime timeInCurrentZone = ZonedDateTime.parse(zonedDate).withZoneSameInstant(currentZoneId);
        return timeInCurrentZone.toLocalDateTime();
    }

    private List<UpdateInfo> parseInfo(String jsonInfo, UpdateInfoType type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode infoNode = objectMapper.readTree(jsonInfo);
            List<UpdateInfo> infos = new ArrayList<>();
            for (JsonNode n : infoNode) {
                switch (type) {
                    case COMMIT:
                        infos.add(parseCommit(n));
                        break;
                    case ISSUE:
                        infos.add(parseIssue(n));
                        break;
                    case COMMENT:
                        infos.add(parseComment(n));
                        break;
                }
            }
            return infos;
        } catch (JsonProcessingException e) {
            throw new GithubResponseJsonIsInvalid("Can't parse JSON response ", e);
        }
    }
}
