package backend.academy.scrapper.exception.custom.client;

import backend.academy.scrapper.exception.custom.ScrapperException;

public class GithubResponseJsonIsInvalid extends ScrapperException {

    public GithubResponseJsonIsInvalid(String message, Throwable cause) {
        super(message, cause);
    }
}
