package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperLinkNotExistsException extends ScrapperException {
    public ScrapperLinkNotExistsException(String message) {
        super(message);
    }
}
