package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.mapper.SqlLinkRowMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class LinkSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<SqlLink> getUncheckedLinksWithBatching(int batchSize, long offset, LocalDateTime minValidation) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("batch", batchSize);
        params.addValue("offset", offset);
        params.addValue("minValidation", Timestamp.valueOf(minValidation));
        return jdbcTemplate.query(
                "SELECT * FROM link" + " WHERE  :minValidation > last_validation" + " LIMIT :batch OFFSET :offset",
                params,
                new SqlLinkRowMapper());
    }

    public SqlLink getLinkById(long id) {
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        String sql = "SELECT * FROM link WHERE id = :id";
        return jdbcTemplate.queryForObject(sql, params, new SqlLinkRowMapper());
    }

    public void deleteLinkById(long id) {
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update("DELETE FROM link WHERE id = :id", params);
    }

    public Long addLink(SqlLink link) {
        MapSqlParameterSource params = new MapSqlParameterSource("url", link.url());
        params.addValue("validation", link.lastValidation());
        String addRequest = "INSERT INTO link (url, last_validation) VALUES (:url, :validation) RETURNING id";
        return jdbcTemplate.queryForObject(addRequest, params, Long.class);
    }

    public void updateLinkValidationById(long id, LocalDateTime lastValidation) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("validation", lastValidation);
        jdbcTemplate.update("UPDATE link SET last_validation = :validation WHERE id = :id", params);
    }

    public Optional<SqlLink> findLinkByUrl(String url) {
        MapSqlParameterSource params = new MapSqlParameterSource("url", url);
        return jdbcTemplate
                .query("SELECT id, url, last_validation FROM link WHERE url = :url", params, new SqlLinkRowMapper())
                .stream()
                .findFirst();
    }
}
