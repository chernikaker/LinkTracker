package backend.academy.scrapper.exception.client;

import backend.academy.scrapper.exception.ScrapperException;

public class GithubResponseJsonIsInvalid extends ScrapperException {

    public GithubResponseJsonIsInvalid(String message, Throwable cause) {
        super(message, cause);
    }
}
