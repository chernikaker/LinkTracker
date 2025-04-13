package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.db.sql.entity.SqlLink;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;

public class SqlLinkRowMapper implements RowMapper<SqlLink> {
    @Override
    public SqlLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String url = rs.getString("url");
        LocalDateTime validation = rs.getTimestamp("last_validation") != null
                ? rs.getTimestamp("last_validation").toLocalDateTime()
                : null;
        return new SqlLink(id, url, validation);
    }
}
