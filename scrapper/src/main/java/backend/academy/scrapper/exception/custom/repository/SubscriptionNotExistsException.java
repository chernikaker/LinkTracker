package backend.academy.scrapper.exception.custom.repository;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class SubscriptionNotExistsException extends ScrapperException {
    public SubscriptionNotExistsException(String message) {
        super(message);
    }
}
