package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.db.sql.entity.SqlTag;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class SqlTagRowMapper implements RowMapper<SqlTag> {
    @Override
    public SqlTag mapRow(ResultSet rs, int rowNum) throws SQLException {
        long tagId = rs.getLong("id");
        String description = rs.getString("tag_text");
        long userId = rs.getLong("user_id");
        return new SqlTag(tagId, description, userId);
    }
}
