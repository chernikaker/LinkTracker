package backend.academy.scrapper.exception.client;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperInternalResponseException extends ScrapperException {
    public ScrapperInternalResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
