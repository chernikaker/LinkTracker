package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = (rs.getLong("id"));
        long chatId = (rs.getLong("chat_id"));
        return new User(id, chatId);
    }
}
