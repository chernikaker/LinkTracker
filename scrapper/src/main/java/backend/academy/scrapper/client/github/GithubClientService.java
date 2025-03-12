package backend.academy.scrapper.client.github;

import static backend.academy.scrapper.client.JsonPathConstant.AUTHOR;
import static backend.academy.scrapper.client.JsonPathConstant.BODY;
import static backend.academy.scrapper.client.JsonPathConstant.COMMIT;
import static backend.academy.scrapper.client.JsonPathConstant.CREATED_AT;
import static backend.academy.scrapper.client.JsonPathConstant.CREATION_DATE;
import static backend.academy.scrapper.client.JsonPathConstant.DATE;
import static backend.academy.scrapper.client.JsonPathConstant.LOGIN;
import static backend.academy.scrapper.client.JsonPathConstant.MESSAGE;
import static backend.academy.scrapper.client.JsonPathConstant.TITLE;
import static backend.academy.scrapper.client.JsonPathConstant.USER;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.client.GithubUnsupportedOptionException;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/** Сервис для работы с клиентом Github */
@Service
@AllArgsConstructor
@Slf4j
public class GithubClientService {

    private final GithubClient githubClient;

    /**
     * Главный метод, получает данные всех доступных типов для ссылки вопроса Github
     *
     * @param link ссылка
     * @return список данных
     */
    public List<UpdateInfo> getAllInfo(Link link) {
        String uri = processUrl(link.url());
        List<UpdateInfo> infoList = new ArrayList<>();
        try {
            // информация о коммитах
            String commitData = githubClient.getResponse(uri.concat("/commits"));
            // информация о проблемах
            String issueData = githubClient.getResponse(uri.concat("/issues"));
            // информация о комментариях
            String commentData = githubClient.getResponse(uri.concat("/comments"));
            infoList.addAll(parseInfo(commentData, UpdateInfoType.COMMENT));
            infoList.addAll(parseInfo(issueData, UpdateInfoType.ISSUE));
            infoList.addAll(parseInfo(commitData, UpdateInfoType.COMMIT));
            return infoList;
        } catch (HttpClientErrorException e) {
            log.atWarn().addKeyValue("link", link.url()).setCause(e).log("Error receiving data from github");
            throw new ScrapperInternalResponseException("Error receiving data from github", e);
        }
    }

    /**
     * Метод проверяет, доступна ли ссылка по API, отправляя на нее запрос
     *
     * @param link проверяемая ссылка
     * @return доступна ли ссылка
     */
    public boolean isLinkAvailable(Link link) {
        try {
            String uri = processUrl(link.url());
            githubClient.getResponse(uri);
            return true;
        } catch (HttpClientErrorException e) {
            log.atWarn().addKeyValue("link", link.url()).setCause(e).log("Error receiving data from github");
            return false;
        }
    }

    /**
     * Обработка одного коммита
     *
     * @param node информация об обновлении
     * @return модель с данными об обновлении
     */
    private UpdateInfo parseCommit(JsonNode node) {
        String message = node.path(COMMIT).path(MESSAGE).asText();
        String committerName = node.path(AUTHOR).path(LOGIN).asText();
        String date = node.path(COMMIT).path(AUTHOR).path(DATE).asText();
        return new UpdateInfo(message, committerName, parseDate(date), UpdateInfoType.COMMIT);
    }

    /**
     * Обработка одной проблемы
     *
     * @param node информация об обновлении
     * @return модель с данными об обновлении
     */
    private UpdateInfo parseIssue(JsonNode node) {
        String message = node.path(TITLE).asText();
        String authorName = node.path(USER).path(LOGIN).asText();
        String date = node.path(CREATED_AT).asText();
        return new UpdateInfo(message, authorName, parseDate(date), UpdateInfoType.ISSUE);
    }

    /**
     * Обработка одного комментария
     *
     * @param node информация об обновлении
     * @return модель с данными об обновлении
     */
    private UpdateInfo parseComment(JsonNode node) {
        String message = node.path(BODY).asText();
        String authorName = node.path(USER).path(LOGIN).asText();
        String date = node.path(CREATED_AT).asText();
        return new UpdateInfo(message, authorName, parseDate(date), UpdateInfoType.COMMENT);
    }

    /**
     * Перевод формата даты из JSON ответа в нужный программе формат
     *
     * @param zonedDate входное время
     * @return дата в формате LocalDateTime
     */
    private LocalDateTime parseDate(String zonedDate) {
        ZoneId currentZoneId = ZoneId.systemDefault();
        ZonedDateTime timeInCurrentZone = ZonedDateTime.parse(zonedDate).withZoneSameInstant(currentZoneId);
        return timeInCurrentZone.toLocalDateTime();
    }

    /**
     * Обработка всех обновлений из JSON сообщения
     *
     * @param jsonInfo JSON с информацией
     * @param type тип обновления
     * @return список обновлений
     */
    private List<UpdateInfo> parseInfo(String jsonInfo, UpdateInfoType type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode infoNode = objectMapper.readTree(jsonInfo);
            List<UpdateInfo> infos = new ArrayList<>();
            for (JsonNode n : infoNode) {
                // логика обработки разная, так как структура JSON отличается
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
                    default:
                        throw new GithubUnsupportedOptionException("Unsupported update info type: " + type);
                }
            }
            return infos;
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.atWarn().addKeyValue("JSON", e).setCause(e).log("Can't parse JSON response");
            throw new ScrapperInternalResponseException("Can't parse JSON response ", e);
        }
    }

    /**
     * Обработка ссылки в формат для работы с API
     *
     * @param url ссылка
     * @return uri для запроса к API
     */
    private String processUrl(String url) {
        // удаление базового url, у API он свой
        return url.replace("https", "http").replace("http://github.com", "repos");
    }
}
