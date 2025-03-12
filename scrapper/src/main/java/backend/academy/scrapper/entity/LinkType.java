package backend.academy.scrapper.entity;

import backend.academy.scrapper.exception.service.ScrapperUnsupportedLinkTypeException;

/** Класс, определяющий возможные типы ссылок */
public enum LinkType {
    GITHUB,
    STACKOVERFLOW;

    /**
     * Метод для определения типа ссылки
     *
     * @param value ссылка
     * @return тип ссылки по внешнему сервису
     * @throws ScrapperUnsupportedLinkTypeException если тип ссылки не соответствует поддерживаемым
     */
    public static LinkType fromValue(String value) {
        String formatted = value.replace("https:", "http:");
        if (formatted.startsWith("http://stackoverflow.com/")) {
            return STACKOVERFLOW;
        } else if (formatted.startsWith("http://github.com/")) {
            return GITHUB;
        }
        throw new ScrapperUnsupportedLinkTypeException("Link type not supported: " + value);
    }
}
