package backend.academy.scrapper.exception.client;

import backend.academy.scrapper.exception.ScrapperException;

public class GithubUnsupportedOptionException extends ScrapperException {
    public GithubUnsupportedOptionException(String message) {
        super(message);
    }
}
