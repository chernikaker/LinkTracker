package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlFilter;
import backend.academy.scrapper.db.sql.mapper.SqlFilterRowMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class FilterSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void addFilterToSubscription(SqlFilter filter) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(filter);
        String sql = "INSERT INTO filter (key, value, subscription_id, user_id)" +
            " VALUES (:key, :value, :subscriptionId, :userId)";
        jdbcTemplate.update(sql, params);
    }

    public void removeFilterById(long filterId) {
        SqlParameterSource params = new MapSqlParameterSource("filterId", filterId);
        String sql = "DELETE FROM filter WHERE id = :filterId";
        jdbcTemplate.update(sql, params);
    }

    public List<SqlFilter> getFiltersBySubscriptionId(long id) {
        SqlParameterSource params = new MapSqlParameterSource("subscriptionId", id);
        String sql = "SELECT * FROM filter WHERE subscription_id = :subscriptionId";
        return jdbcTemplate.query(sql, params, new SqlFilterRowMapper());
    }

    public void removeFiltersBySubscriptionId(long id) {
        SqlParameterSource params = new MapSqlParameterSource("subscriptionId", id);
        String sql = "DELETE FROM filter WHERE subscription_id = :subscriptionId";
        jdbcTemplate.update(sql, params);
    }
}
