package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class SubscriptionNotExistsException extends ScrapperException {
    public SubscriptionNotExistsException(String message) {
        super(message);
    }
}
