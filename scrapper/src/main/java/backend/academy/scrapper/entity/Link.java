package backend.academy.scrapper.entity;

import backend.academy.scrapper.exception.service.ScrapperUnsupportedLinkTypeException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Класс сущность для объекта ссылки */
@Getter
@Setter
@AllArgsConstructor
public class Link {

    String url;
    LinkType type;
    // время получения последнего обновления информации о ссылке
    LocalDateTime lastValidation;

    /**
     * Метод для определения типа ссылки
     *
     * @param url ссылка
     * @return тип ссылки по внешнему сервису
     * @throws ScrapperUnsupportedLinkTypeException если тип ссылки не соответствует поддерживаемым
     */
    public static LinkType getLinkType(String url) {
        String formatted = url.replace("https:", "http:");
        if (formatted.startsWith("http://stackoverflow.com/")) {
            return LinkType.STACKOVERFLOW;
        } else if (formatted.startsWith("http://github.com/")) {
            return LinkType.GITHUB;
        }
        throw new ScrapperUnsupportedLinkTypeException("Link type not supported: " + url);
    }
}
