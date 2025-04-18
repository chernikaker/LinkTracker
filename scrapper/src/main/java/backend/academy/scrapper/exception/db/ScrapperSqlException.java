package backend.academy.scrapper.exception.db;

public class ScrapperSqlException extends ScrapperDbException {

    public ScrapperSqlException(String message) {
        super(message);
    }

    public ScrapperSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
