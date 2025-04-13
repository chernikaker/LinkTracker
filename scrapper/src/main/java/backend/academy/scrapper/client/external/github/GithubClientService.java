package backend.academy.scrapper.client.external.github;

import static backend.academy.scrapper.client.JsonPathConstant.BODY;
import static backend.academy.scrapper.client.JsonPathConstant.CREATED_AT;
import static backend.academy.scrapper.client.JsonPathConstant.LOGIN;
import static backend.academy.scrapper.client.JsonPathConstant.TITLE;
import static backend.academy.scrapper.client.JsonPathConstant.USER;

import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.entity.Link;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/** Сервис для работы с клиентом Github */
@Service
@Slf4j
public class GithubClientService {

    private final ExternalClient client;

    public GithubClientService(@Qualifier("githubClient") ExternalClient client) {
        this.client = client;
    }

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
            // информация о проблемах
            String issueData = client.getResponse(uri.concat("/issues"));
            // информация о пулл реквестах
            String prData = client.getResponse(uri.concat("/pulls"));
            infoList.addAll(parseInfo(prData, UpdateInfoType.PULL_REQUEST));
            infoList.addAll(parseInfo(issueData, UpdateInfoType.ISSUE));
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
            client.getResponse(uri);
            return true;
        } catch (HttpClientErrorException e) {
            log.atWarn().addKeyValue("link", link.url()).setCause(e).log("Error receiving data from github");
            return false;
        }
    }

    /**
     * Обработка обновления одного типа
     *
     * @param node информация об обновлении
     * @return модель с данными об обновлении
     */
    private UpdateInfo parseItem(JsonNode node, UpdateInfoType type) {
        String title = node.path(TITLE).asText();
        String message = node.path(BODY).asText();
        message = message.length() > 200 ? message.substring(0, 200) : message;
        String authorName = node.path(USER).path(LOGIN).asText();
        String date = node.path(CREATED_AT).asText();
        return new UpdateInfo(title, message, authorName, parseDate(date), type);
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
                infos.add(parseItem(n, type));
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
