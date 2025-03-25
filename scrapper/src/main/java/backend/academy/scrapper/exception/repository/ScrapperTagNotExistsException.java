package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperTagNotExistsException extends ScrapperException {
    public ScrapperTagNotExistsException(String message) {
        super(message);
    }
}
