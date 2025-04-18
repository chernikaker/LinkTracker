package backend.academy.scrapper.exception.db;

import backend.academy.scrapper.exception.ScrapperException;
import org.springframework.http.HttpStatus;

public class ScrapperSqlException extends ScrapperDbException {

    public ScrapperSqlException(String message) {
        super(message);
    }

    public ScrapperSqlException(String message, Throwable cause) {
        super(message, cause);
    }

}
