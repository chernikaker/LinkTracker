package backend.academy.scrapper.exception.client;

import backend.academy.scrapper.exception.ScrapperException;

public class StackoverflowResponseJsonIsInvalid extends ScrapperException {

    public StackoverflowResponseJsonIsInvalid(String message, Throwable cause) {
        super(message, cause);
    }
}
