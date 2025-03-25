package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlFilter;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.mapper.SqlFilterRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.List;

@Repository
public class FilterSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public FilterSqlRepository(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public long addFilterToSubscription(SqlFilter filter) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(filter);
        String sql = "INSERT INTO filter (key, value, subscription_id, user_id)" +
            " VALUES (:key, :value, :subscriptionId, :userId) RETURNING id";
        return jdbcTemplate.update(sql, params);
    }

    public void removeFilterById(long filterId) {
        SqlParameterSource params = new MapSqlParameterSource("filterId", filterId);
        String sql = "DELETE FROM filter WHERE id = :filterId";
        jdbcTemplate.update(sql, params);
    }

    public List<SqlFilter> getFiltersBySubscriptionId(long id) {
        SqlParameterSource params = new MapSqlParameterSource("subscriptionId", id);
        String sql = "SELECT * FROM filter WHERE subscription_id = :id";
        return jdbcTemplate.query(sql, params, new SqlFilterRowMapper());
    }
}
