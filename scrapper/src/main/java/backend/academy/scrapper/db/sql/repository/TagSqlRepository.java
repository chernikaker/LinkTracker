package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlTag;
import backend.academy.scrapper.db.sql.mapper.SqlTagRowMapper;
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
public class TagSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long addTag(SqlTag tag) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(tag);
        String query = "INSERT INTO tag (tag_text, user_id) VALUES (:tagText, :userId) RETURNING id";
        return jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public List<SqlTag> getUserTagsById(long userId) {
        SqlParameterSource params = new MapSqlParameterSource("userId", userId);
        String query = "SELECT * FROM tag WHERE user_id = :userId";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper());
    }

    public List<SqlTag> getSubscriptionTags(long subscrId) {
        SqlParameterSource params = new MapSqlParameterSource("subscrId", subscrId);
        String query = "SELECT t.id, t.tag_text, t.user_id FROM subscription_tag st " + "JOIN tag t ON t.id = st.tag_id"
                + " WHERE st.subscription_id = :subscrId";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper());
    }

    public Optional<SqlTag> getTagByTextAndUserId(long userId, String tagText) {
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        params.addValue("tagText", tagText);
        String query = "SELECT * FROM tag WHERE tag_text = :tagText AND user_id = :userId";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper()).stream().findFirst();
    }

    public Long removeTagById(long tagId) {
        SqlParameterSource params = new MapSqlParameterSource("tagId", tagId);
        String query = "DELETE FROM tag WHERE id = :tagId RETURNING id";
        return jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public void removeTagFromSubscription(long tagId, long subscrId) {
        MapSqlParameterSource params = new MapSqlParameterSource("tagId", tagId);
        params.addValue("subscrId", subscrId);
        String query = "DELETE FROM subscription_tag WHERE tag_id = :tagId AND subscription_id = :subscrId";
        jdbcTemplate.update(query, params);
    }

    public void addTagToSubscription(long tagId, long subscrId) {
        MapSqlParameterSource params = new MapSqlParameterSource("tagId", tagId);
        params.addValue("subscrId", subscrId);
        String query = "INSERT INTO subscription_tag (subscription_id, tag_id) VALUES (:subscrId, :tagId) ON CONFLICT (subscription_id, tag_id) DO NOTHING";
        jdbcTemplate.update(query, params);
    }

    public void removeAllTagsFromSubscriptionById(long subscrId) {
        SqlParameterSource params = new MapSqlParameterSource("subscrId", subscrId);
        String query = "DELETE FROM subscription_tag WHERE subscription_id = :subscrId";
        jdbcTemplate.update(query, params);
    }

    public Optional<SqlTag> getTagByValue(String value) {
        SqlParameterSource params = new MapSqlParameterSource("value", value);
        String query = "SELECT * FROM tag WHERE tag_text = :value";
        return jdbcTemplate.query(query, params, new SqlTagRowMapper()).stream().findFirst();
    }
}
