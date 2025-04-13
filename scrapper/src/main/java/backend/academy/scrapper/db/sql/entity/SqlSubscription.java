package backend.academy.scrapper.db.sql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class SqlSubscription {
    private long id;
    private long userId;
    private long linkId;

    public SqlSubscription(long userId, long linkId) {
        this(0, userId, linkId);
    }
}
