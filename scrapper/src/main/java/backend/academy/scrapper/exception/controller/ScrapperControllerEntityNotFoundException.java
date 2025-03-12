package backend.academy.scrapper.exception.controller;

import backend.academy.scrapper.exception.ScrapperException;
import org.springframework.http.HttpStatus;

public class ScrapperControllerEntityNotFoundException extends ScrapperException {

    @Override
    public int getStatus() {
        return HttpStatus.NOT_FOUND.value();
    }

    public ScrapperControllerEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
