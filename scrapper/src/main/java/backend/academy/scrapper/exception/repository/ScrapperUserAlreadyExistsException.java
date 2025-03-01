package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperUserAlreadyExistsException extends ScrapperException {
    public ScrapperUserAlreadyExistsException(String message) {
        super(message);
    }
}
