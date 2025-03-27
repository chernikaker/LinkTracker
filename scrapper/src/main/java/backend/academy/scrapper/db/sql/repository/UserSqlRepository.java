package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.mapper.SqlUserRowMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long addUser(SqlUser user) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        String query = "INSERT INTO tg_user (chat_id) VALUES (:chatId) RETURNING id";
        return jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public void deleteUserById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        String query = "DELETE FROM tg_user WHERE id = :id";
        jdbcTemplate.update(query, params);
    }

    public Optional<SqlUser> findUserByChatId(long chatId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);
        String query = "SELECT * FROM tg_user WHERE chat_id = :chatId";
        return jdbcTemplate.query(query, params, new SqlUserRowMapper())
            .stream()
            .findFirst();
    }

    public Optional<SqlUser> findUserById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        String query = "SELECT * FROM tg_user WHERE chat_id = :id";
        return jdbcTemplate.query(query, params, new SqlUserRowMapper())
            .stream()
            .findFirst();
    }
}
