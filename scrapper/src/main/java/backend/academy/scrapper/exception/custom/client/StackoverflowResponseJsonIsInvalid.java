package backend.academy.scrapper.exception.custom.client;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class StackoverflowResponseJsonIsInvalid extends ScrapperException {

    public StackoverflowResponseJsonIsInvalid(String message, Throwable cause) {
        super(message, cause);
    }
}
