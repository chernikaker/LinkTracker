package backend.academy.dto;

import java.util.List;

/**
 * DTO ответа на запрос добавления подписки на ссылку
 *
 * @param id id подписки
 * @param url ссылка
 * @param tags тэги
 * @param filters фильтры
 */
public record LinkResponse(long id, String url, List<String> tags, List<String> filters) {}
