package backend.academy.scrapper.exception.controller;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperInvalidIdException extends ScrapperException {
    public ScrapperInvalidIdException(String message) {
        super(message);
    }
}
