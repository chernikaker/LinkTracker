package backend.academy.scrapper.exception.custom.controller;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class ScrapperInvalidIdException extends ScrapperException {
    public ScrapperInvalidIdException(String message) {
        super(message);
    }
}
