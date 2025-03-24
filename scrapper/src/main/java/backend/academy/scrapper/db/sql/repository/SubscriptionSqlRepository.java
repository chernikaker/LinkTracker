package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.mapper.SqlSubscriptionRowMapper;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SubscriptionSqlRepository(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Long addSubscription(SqlSubscription subscription) {
        SqlParameterSource src = new BeanPropertySqlParameterSource(subscription);
        String query = "INSERT INTO subscription (user_id, link_id) VALUES (:userId, :linkId) RETURNING id";
        return jdbcTemplate.queryForObject(query, src, Long.class);
    }

    public void deleteSubscriptionById(long id) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update("DELETE FROM subscription WHERE id = :id", namedParameters);
    }

    public List<Long> deleteUserSubscriptionsById(long userId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", userId);
        String query = "DELETE FROM subscription WHERE user_id = :id RETURNING link_id";
        return jdbcTemplate.queryForList(query, namedParameters, Long.class);
    }

    public List<SqlSubscription> getSubscriptionsByUserId(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", userId);
        String query = "SELECT * FROM subscription WHERE user_id = :id";
        return jdbcTemplate.query(query, params, new SqlSubscriptionRowMapper());
    }

    public List<SqlSubscription> getSubscriptionsByLink(long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", linkId);
        String query = "SELECT * FROM subscription WHERE link_id = :id";
        return jdbcTemplate.query(query, params, new SqlSubscriptionRowMapper());
    }

    public Optional<SqlSubscription> getSubscriptionByLinkAndUserId(long linkId, long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("userId", userId);
        String query = "SELECT * FROM subscription WHERE link_id = :linkId AND user_id = :userId";
        return jdbcTemplate.query(query, params, new SqlSubscriptionRowMapper())
            .stream()
            .findFirst();
    }
}
