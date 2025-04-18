package backend.academy.scrapper.exception.db;

import backend.academy.scrapper.exception.ScrapperException;
import org.springframework.http.HttpStatus;

public class ScrapperOrmException extends ScrapperDbException {

    public ScrapperOrmException(String message) {
        super(message);
    }

    public ScrapperOrmException(String message, Throwable cause) {
        super(message, cause);
    }

}
