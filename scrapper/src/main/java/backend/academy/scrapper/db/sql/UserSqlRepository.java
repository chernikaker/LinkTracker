package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.UserRepository;
import backend.academy.scrapper.db.sql.mapper.UserRowMapper;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class UserSqlRepository implements UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserSqlRepository(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Long addUser(User user) {
        Optional<User> existingUser = findUserByChatId(user.chatId());
        if (existingUser.isEmpty()) {
            throw new ScrapperUserAlreadyExistsException("User " + user.chatId() + " already exists");
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", user.chatId());
        String query = "INSERT INTO tg_user (chat_id) VALUES (:chatId) RETURNING id";
        return jdbcTemplate.queryForObject(query, params, Long.class);
    }

    @Override
    public User getUserByChatId(long chatId) {
        Optional<User> existingUser = findUserByChatId(chatId);
        if (existingUser.isEmpty()) {
            throw new ScrapperUserNotExistsException("User " + chatId + " does not exist");
        }
        return existingUser.get();
    }

    @Override
    public void removeUserById(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", userId);
        String query = "DELETE FROM tg_user WHERE id = :id";
        jdbcTemplate.update(query, params);
    }

    private Optional<User> findUserByChatId(long chatId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);
        String query = "SELECT * FROM tg_user WHERE chat_id = :chatId";
        return jdbcTemplate.query(query, params, new UserRowMapper())
            .stream()
            .findFirst();
    }
}
