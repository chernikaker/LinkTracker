package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlTag;
import javax.sql.DataSource;
import backend.academy.scrapper.db.sql.mapper.SqlTagRowMapper;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TagSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long addTag(SqlTag tag) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(tag);
        String query = "INSERT INTO tag (tag_text, user_id) VALUES (:tagText, :userId) RETURNING id";
        return jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public List<SqlTag> getUserTagsById(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        String query = "SELECT * FROM tag WHERE user_id = :userId";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper());
    }

    public List<SqlTag> getSubscriptionTags(long subscriptionId) {
        MapSqlParameterSource params = new MapSqlParameterSource("subscriptionId", subscriptionId);
        String query = "SELECT t.id, t.tag_text, t.user_id FROM subscription_tag st " +
            "JOIN tag t ON t.id = st.tag_id" +
            " WHERE st.subscription_id = :subscriptionId";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper());
    }

    public Optional<SqlTag> getTagByTextAndUserId(long userId, String tagText) {
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        params.addValue("tagText", tagText);
        String query = "SELECT * FROM tag WHERE tag_text = :tagText AND user_id = :userId";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper())
            .stream()
            .findFirst();
    }

    public Long removeTagById(long tagId) {
        SqlParameterSource params = new MapSqlParameterSource("tagId", tagId);
        String query = "DELETE FROM tag WHERE id = :tagId RETURNING id";
        return jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public void removeTagFromSubscription(long tagId, long subscriptionId) {
        MapSqlParameterSource params = new MapSqlParameterSource("tagId", tagId);
        params.addValue("subscriptionId", subscriptionId);
        String query = "DELETE FROM subscription_tag WHERE tag_id = :tagId AND subscription_id = :subscriptionId";
        jdbcTemplate.update(query, params);
    }

    public void addTagToSubscription(long tagId, long subscriptionId) {
        MapSqlParameterSource params = new MapSqlParameterSource("tagId", tagId);
        params.addValue("subscriptionId", subscriptionId);
        String query = "INSERT INTO subscription_tag (subscription_id, tag_id) VALUES (:subscriptionId, :tagId)";
        jdbcTemplate.update(query, params);
    }

    public void removeAllTagsFromSubscriptionById(long subscriptionId) {
        MapSqlParameterSource params = new MapSqlParameterSource("subscriptionId", subscriptionId);
        String query = "DELETE FROM subscription_tag WHERE tag_id = :tagId";
        jdbcTemplate.update(query, params);
    }
}
