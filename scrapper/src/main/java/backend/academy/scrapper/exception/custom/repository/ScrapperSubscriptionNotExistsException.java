package backend.academy.scrapper.exception.custom.repository;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperSubscriptionNotExistsException extends ScrapperException {
    public ScrapperSubscriptionNotExistsException(String message) {
        super(message);
    }
}
