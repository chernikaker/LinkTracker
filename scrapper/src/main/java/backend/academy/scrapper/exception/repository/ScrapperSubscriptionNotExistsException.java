package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperSubscriptionNotExistsException extends ScrapperException {
    public ScrapperSubscriptionNotExistsException(String message) {
        super(message);
    }
}
