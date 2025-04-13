package backend.academy.scrapper.db.sql.entity;

public record SqlUser(long id, long chatId) {

    public SqlUser(long chatId) {
        this(0, chatId);
    }
}
