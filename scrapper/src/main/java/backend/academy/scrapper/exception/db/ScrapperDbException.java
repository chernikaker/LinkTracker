package backend.academy.scrapper.exception.db;

import backend.academy.scrapper.exception.ScrapperException;
import org.springframework.http.HttpStatus;

public abstract class ScrapperDbException extends ScrapperException {

    public ScrapperDbException(String message) {
        super(message);
    }

    public ScrapperDbException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
