package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlSubscriptionRowMapper implements RowMapper<SqlSubscription> {
    @Override
    public SqlSubscription mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        long linkId = rs.getLong("link_id");
        long userId = rs.getLong("user_id");
        return new SqlSubscription(id, userId, linkId);
    }
}
