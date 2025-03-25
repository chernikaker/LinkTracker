package backend.academy.scrapper.db.sql.entity;

public record SqlTag(long id, String tagText, long userId) {

    public SqlTag(String text, long userId){
        this(0, text, userId);
    }
}
