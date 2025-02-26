package backend.academy.scrapper.exception.custom;

public class ScrapperUserAlreadyExistsException extends ScrapperException {
    public ScrapperUserAlreadyExistsException(String message) {
        super(message);
    }
}
