package backend.academy.scrapper.exception.custom.repository;


import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperUserNotExistsException extends ScrapperException {

    public ScrapperUserNotExistsException(String message) {
        super(message);
    }
}
