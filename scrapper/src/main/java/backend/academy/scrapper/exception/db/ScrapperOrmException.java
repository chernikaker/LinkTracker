package backend.academy.scrapper.exception.db;

public class ScrapperOrmException extends ScrapperDbException {

    public ScrapperOrmException(String message) {
        super(message);
    }

    public ScrapperOrmException(String message, Throwable cause) {
        super(message, cause);
    }
}
