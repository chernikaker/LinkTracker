package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperUserNotExistsException extends ScrapperException {

    public ScrapperUserNotExistsException(String message) {
        super(message);
    }
}
