package backend.academy.scrapper.exception.repository;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperSubscriptionAlreadyExistsException extends ScrapperException {
    public ScrapperSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
