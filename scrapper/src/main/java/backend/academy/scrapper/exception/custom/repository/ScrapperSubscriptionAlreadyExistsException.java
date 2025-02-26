package backend.academy.scrapper.exception.custom.repository;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperSubscriptionAlreadyExistsException extends ScrapperException {
    public ScrapperSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
