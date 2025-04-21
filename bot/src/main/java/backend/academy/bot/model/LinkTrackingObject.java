package backend.academy.bot.model;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.telegram.handler.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Объект для хранения временной информации о текущем вводе пользователя до отправки в Scrapper
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
    private UserState state;
    private Command command;
}
