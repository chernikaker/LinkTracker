package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.db.sql.entity.SqlFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class SqlFilterRowMapper implements RowMapper<SqlFilter> {
    @Override
    public SqlFilter mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        long subscriptionId = rs.getLong("subscription_id");
        long userId = rs.getLong("user_id");
        String key = rs.getString("key");
        String value = rs.getString("value");
        return new SqlFilter(id, key, value, subscriptionId, userId);
    }
}
