package backend.academy.scrapper.client.external.stackoverflow;

import static backend.academy.scrapper.client.JsonPathConstant.CREATION_DATE;
import static backend.academy.scrapper.client.JsonPathConstant.DISPLAY_NAME;
import static backend.academy.scrapper.client.JsonPathConstant.OWNER;

import backend.academy.scrapper.client.external.ExternalClient;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/** Сервис для работы с клиентом Stackoverflow */
@Service
@Slf4j
public class StackoverflowClientService {

    private final ExternalClient client;

    public StackoverflowClientService(@Qualifier("stackoverflowClient") ExternalClient client) {
        this.client = client;
    }

    /**
     * Главный метод, получает данные всех доступных типов для ссылки вопроса Stackoverflow
     *
     * @param link ссылка
     * @return список данных
     */
    public List<UpdateInfo> getAllInfo(Link link) {
        String uri = processUrl(link.url());
        List<UpdateInfo> infoList = new ArrayList<>();
        try {
            // информация о комментариях
            String commentData = client.getResponse(uri.concat("/comments"));
            // информация об ответах
            String answerData = client.getResponse(uri.concat("/answers"));
            infoList.addAll(parseInfo(commentData, UpdateInfoType.COMMENT));
            infoList.addAll(parseInfo(answerData, UpdateInfoType.ANSWER));
            return infoList;
        } catch (HttpClientErrorException e) {
            log.atWarn().addKeyValue("link", link.url()).setCause(e).log("Error receiving data from stackoverflow");
            throw new ScrapperInternalResponseException("Error receiving data from stackoverflow", e);
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
            // если возникла ошибка, ссылку нельзя считать доступной
            log.atWarn().addKeyValue("link", link.url()).setCause(e).log("Error receiving data from stackoverflow");
            return false;
        }
    }

    /**
     * Обработка одного потенциального обновления (ответ на вопрос, комментарий)
     *
     * @param node информация об обновлении
     * @param type тип обновления
     * @return модель с данными об обновлении
     */
    private UpdateInfo parseItem(JsonNode node, UpdateInfoType type) {
        String message = "";
        String authorName = node.path(OWNER).path(DISPLAY_NAME).asText();
        long date = node.path(CREATION_DATE).asLong();
        return new UpdateInfo(message, authorName, parseDate(date), type);
    }

    /**
     * Перевод формата даты из JSON ответа в нужный программе формат
     *
     * @param timestamp входное время
     * @return дата в формате LocalDateTime
     */
    private LocalDateTime parseDate(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
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
        String uri = url.replace("https:", "http:").replace("http://stackoverflow.com", "");
        // если в ссылке было текстовое название вопроса, удаляем его
        if (!Character.isDigit(uri.charAt(uri.length() - 1))) {
            uri = uri.substring(0, uri.lastIndexOf("/"));
        }
        return uri;
    }
}
