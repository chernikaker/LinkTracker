package backend.academy.bot.model;

import backend.academy.bot.cache.InMemoryTrackingCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Объект для хранения временной информации о ссылке (довалении или удалении) до отправки в Scrapper. Содержит текущее
 * состояние ввода пользователя
 *
 * @see InMemoryTrackingCache
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkTrackingObject {

    private String link;
    private String[] tags;
    private String[] filters;
    private UserState state = UserState.TRACKING_LINK;
}
