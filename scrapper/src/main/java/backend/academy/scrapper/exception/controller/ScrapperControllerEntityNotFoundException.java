package backend.academy.scrapper.exception.controller;

import backend.academy.scrapper.exception.ScrapperException;

public class ScrapperControllerEntityNotFoundException extends ScrapperException {

    @Override
    public int getStatus() {
        return 404;
    }

    public ScrapperControllerEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
