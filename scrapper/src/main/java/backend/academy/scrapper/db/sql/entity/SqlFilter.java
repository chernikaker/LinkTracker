package backend.academy.scrapper.db.sql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SqlFilter {

    private long id;
    private String key;
    private String value;
    private long subscriptionId;
    private long userId;
}
