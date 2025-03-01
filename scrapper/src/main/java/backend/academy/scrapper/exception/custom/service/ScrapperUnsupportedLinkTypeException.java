package backend.academy.scrapper.exception.custom.service;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperUnsupportedLinkTypeException extends ScrapperException {
    public ScrapperUnsupportedLinkTypeException(String message) {
        super(message);
    }
}
