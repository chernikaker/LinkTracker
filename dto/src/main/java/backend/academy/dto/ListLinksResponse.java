package backend.academy.dto;

import java.util.List;

/**
 * * DTO ответа на запрос получения списка ссылок пользователя
 *
 * @param links список ссылок
 * @param size размер списка
 */
public record ListLinksResponse(List<LinkResponse> links, int size) {}
