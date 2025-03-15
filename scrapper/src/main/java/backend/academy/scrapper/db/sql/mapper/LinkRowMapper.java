package backend.academy.scrapper.db.sql.mapper;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LinkRowMapper implements RowMapper<Link> {
    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = (rs.getLong("id"));
        String url = (rs.getString("url"));
        LinkType type = LinkType.fromValue(url);
        LocalDateTime validation = (rs.getTimestamp("last_validation") != null ?
            rs.getTimestamp("last_validation").toLocalDateTime() : null);
        return new Link(id, url, type, validation);
    }
}
