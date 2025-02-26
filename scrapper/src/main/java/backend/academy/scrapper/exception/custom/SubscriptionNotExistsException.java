package backend.academy.scrapper.exception.custom;

public class SubscriptionNotExistsException extends ScrapperException {
    public SubscriptionNotExistsException(String message) {
        super(message);
    }
}
