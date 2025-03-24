package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.db.sql.entity.SqlTag;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlTagRowMapper implements RowMapper<SqlTag> {
    @Override
    public SqlTag mapRow(ResultSet rs, int rowNum) throws SQLException {
        long tagId = rs.getLong("id");
        String description = rs.getString("description");
        long userId = rs.getLong("user_id");
        return new SqlTag(tagId, description, userId);
    }
}
