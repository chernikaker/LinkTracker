package backend.academy.scrapper.exception.custom;

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
}
