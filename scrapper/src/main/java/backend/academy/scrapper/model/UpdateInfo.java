package backend.academy.scrapper.model;

import java.time.LocalDateTime;

/**
 * Внутренняя модель данных в Scrapper. Информация о обновлении ресурса
 *
 * @param message сообщение обновления
 * @param authorName имя автора обновления
 * @param time время обновления
 * @param type тип обновления
 */
public record UpdateInfo(String message, String authorName, LocalDateTime time, UpdateInfoType type) {}
