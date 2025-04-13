package backend.academy.scrapper.db.sql.entity;

public record SqlFilter(long id, String key, String value, long subscriptionId, long userId) {

    public SqlFilter(String key, String value, long subscriptionId, long userId) {
        this(0, key, value, subscriptionId, userId);
    }
}
