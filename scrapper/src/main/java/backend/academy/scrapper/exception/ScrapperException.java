package backend.academy.scrapper.exception;

import lombok.Getter;

@Getter
public class ScrapperException extends RuntimeException {

    private final String description = "Scrapper service exception";

    public int getStatus() {
        return 400;
    }

    public ScrapperException(String message) {
        super(message);
    }

    public ScrapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
