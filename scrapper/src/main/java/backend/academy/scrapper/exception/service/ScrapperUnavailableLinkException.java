package backend.academy.scrapper.exception.service;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperUnavailableLinkException extends ScrapperException {
    public ScrapperUnavailableLinkException(String message) {
        super(message);
    }
}
