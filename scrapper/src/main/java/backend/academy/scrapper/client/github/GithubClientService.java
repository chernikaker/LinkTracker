package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.dto.UpdateInfoType;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.client.GithubResponseJsonIsInvalid;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@AllArgsConstructor
@Slf4j
public class GithubClientService {

    private final GithubClient githubClient;

    public List<UpdateInfo> getAllInfo(Link link) {
        String uri = processUrl(link.url());
        List<UpdateInfo> infoList = new ArrayList<>();
        try {
            String commitData = githubClient.getResponse(uri.concat("/commits"));
            String issueData = githubClient.getResponse(uri.concat("/issues"));
            String commentData = githubClient.getResponse(uri.concat("/comments"));
            infoList.addAll(parseInfo(commentData, UpdateInfoType.COMMENT));
            infoList.addAll(parseInfo(issueData, UpdateInfoType.ISSUE));
            infoList.addAll(parseInfo(commitData, UpdateInfoType.COMMIT));
            return infoList;
        } catch (HttpClientErrorException e) {
            log.error("Error receiving data from github {}", e.getResponseBodyAsString());
            throw new ScrapperInternalResponseException("Error receiving data from github", e);
        }
    }

    public boolean isLinkAvailable(Link link) {
        try {
            String uri = processUrl(link.url());
            githubClient.getResponse(uri);
            return true;
        } catch (HttpClientErrorException e) {
            return (!e.getStatusCode().is4xxClientError());
        }
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

    private String processUrl(String url) {
        return url.startsWith("https")
                ? url.replace("https://github.com", "repos")
                : url.replace("http://github.com", "repos");
    }
}
