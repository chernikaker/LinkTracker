package backend.academy.scrapper.exception.custom.repository;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperLinkNotExistsException extends ScrapperException {
    public ScrapperLinkNotExistsException(String message) {
        super(message);
    }
}
