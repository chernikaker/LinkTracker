package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.db.sql.entity.SqlUser;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class SqlUserRowMapper implements RowMapper<SqlUser> {
    @Override
    public SqlUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = (rs.getLong("id"));
        long chatId = (rs.getLong("chat_id"));
        return new SqlUser(id, chatId);
    }
}
