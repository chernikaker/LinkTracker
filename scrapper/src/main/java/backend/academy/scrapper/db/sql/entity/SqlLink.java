package backend.academy.scrapper.db.sql.entity;

import java.time.LocalDateTime;

public record SqlLink(long id, String url, LocalDateTime lastValidation) {

    public SqlLink(String url, LocalDateTime lastValidation) {
        this(0, url, lastValidation);
    }
}
