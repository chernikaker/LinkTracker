package backend.academy.scrapper.exception.custom.repository;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperUserAlreadyExistsException extends ScrapperException {
    public ScrapperUserAlreadyExistsException(String message) {
        super(message);
    }
}
