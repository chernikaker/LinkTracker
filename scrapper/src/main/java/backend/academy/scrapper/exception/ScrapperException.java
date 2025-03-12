package backend.academy.scrapper.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ScrapperException extends RuntimeException {

    private final String description = "Scrapper service exception";

    public int getStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }

    public ScrapperException(String message) {
        super(message);
    }

    public ScrapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
