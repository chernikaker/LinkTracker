package backend.academy.scrapper.exception.custom.service;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperUnavailableLinkException extends ScrapperException {
    public ScrapperUnavailableLinkException(String message) {
        super(message);
    }
}
