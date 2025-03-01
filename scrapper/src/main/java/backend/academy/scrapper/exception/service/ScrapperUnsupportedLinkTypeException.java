package backend.academy.scrapper.exception.service;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperUnsupportedLinkTypeException extends ScrapperException {
    public ScrapperUnsupportedLinkTypeException(String message) {
        super(message);
    }
}
