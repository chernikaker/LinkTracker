package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.mapper.SqlSubscriptionRowMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class SubscriptionSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long addSubscription(SqlSubscription subscription) {
        SqlParameterSource src = new BeanPropertySqlParameterSource(subscription);
        String query = "INSERT INTO subscription (user_id, link_id) VALUES (:userId, :linkId) RETURNING id";
        return jdbcTemplate.queryForObject(query, src, Long.class);
    }

    public void deleteSubscriptionById(long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource("id", id);
        jdbcTemplate.update("DELETE FROM subscription WHERE id = :id", namedParameters);
    }

    public List<SqlSubscription> getSubscriptionsByUserId(long userId) {
        SqlParameterSource params = new MapSqlParameterSource("id", userId);
        String query = "SELECT * FROM subscription WHERE user_id = :id";
        return jdbcTemplate.query(query, params, new SqlSubscriptionRowMapper());
    }

    public List<SqlSubscription> getSubscriptionsByLink(long linkId) {
        SqlParameterSource params = new MapSqlParameterSource("id", linkId);
        String query = "SELECT * FROM subscription WHERE link_id = :id";
        return jdbcTemplate.query(query, params, new SqlSubscriptionRowMapper());
    }

    public List<Long> getChatsByLink(long linkId) {
        SqlParameterSource params = new MapSqlParameterSource("id", linkId);
        String query = "SELECT u.chat_id FROM subscription s JOIN tg_user u ON u.id = s.user_id WHERE s.link_id = :id";
        return jdbcTemplate.queryForList(query, params, Long.class);
    }

    public Optional<SqlSubscription> getSubscriptionByLinkAndUserId(long linkId, long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("userId", userId);
        String query = "SELECT * FROM subscription WHERE link_id = :linkId AND user_id = :userId";
        return jdbcTemplate.query(query, params, new SqlSubscriptionRowMapper()).stream()
                .findFirst();
    }
}
