package backend.academy.scrapper.client.stackoverflow;

import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.dto.UpdateInfoType;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.custom.client.GithubResponseJsonIsInvalid;
import backend.academy.scrapper.exception.custom.client.StackoverflowResponseJsonIsInvalid;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class StackoverflowClientService {

    private final StackoverflowClient client;

    public List<UpdateInfo> getAllInfo(Link link)
    {
        String uri = link.url().replace("https://stackoverflow.com", "");
        List<UpdateInfo> infoList = new ArrayList<>();
        String commentData = client.getResponse(uri.concat("/comments"));
        String answerData = client.getResponse(uri.concat("/answers"));
        infoList.addAll(parseInfo(commentData, UpdateInfoType.COMMENT));
        infoList.addAll(parseInfo(answerData, UpdateInfoType.ANSWER));
        return infoList;
    }

    private UpdateInfo parseItem(JsonNode node, UpdateInfoType type) {
        String message = "";
        String authorName = node.path("owner").path("display_name").asText();
        long date = node.path("creation_date").asLong();
        return new UpdateInfo(message, authorName, parseDate(date), type);
    }

    private LocalDateTime parseDate(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private List<UpdateInfo> parseInfo(String jsonInfo, UpdateInfoType type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode infoNode = objectMapper.readTree(jsonInfo);
            List<UpdateInfo> infos = new ArrayList<>();
            for (JsonNode n : infoNode) {
                infos.add(parseItem(n, type));
            }
            return infos;
        } catch (JsonProcessingException e) {
            throw new StackoverflowResponseJsonIsInvalid("Can't parse JSON response ", e);
        }
    }
}
