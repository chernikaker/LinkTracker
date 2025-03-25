package backend.academy.scrapper.db.sql.repository;

import backend.academy.scrapper.db.sql.mapper.LinkRowMapper;
import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LinkSqlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LinkSqlRepository(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Link> getUncheckedLinksWithBatching(int batchSize, long offset, long duration){
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("batch", batchSize);
        namedParameters.addValue("offset", offset);
        namedParameters.addValue("duration", duration);
        return jdbcTemplate.query(
            "SELECT * FROM link WHERE CURRENT_TIMESTAMP-CAST(:duration || ' seconds' AS INTERVAL) > last_validation LIMIT :batch OFFSET :offset  ",
            namedParameters,
            new LinkRowMapper()
        );
    }

    public Link getLinkById(long id) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", id);
        String sql = "SELECT * FROM link WHERE id = :id";
        return jdbcTemplate.queryForObject(sql, namedParameters, new LinkRowMapper());
    }

    public void deleteLinkById(long id) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update("DELETE FROM link WHERE id = :id", namedParameters);
    }

    public Long addLink(Link link) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("url", link.url());
        namedParameters.addValue("lastValidation", link.lastValidation());
        String addRequest = "INSERT INTO link (url, last_validation) VALUES (:url, :lastValidation) RETURNING id";
        return jdbcTemplate.queryForObject(addRequest, namedParameters, Long.class);
    }


    public void updateLinkValidationById(long id, LocalDateTime lastValidation) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", id);
        namedParameters.addValue("lastValidation", lastValidation);
        jdbcTemplate.update("UPDATE link SET last_validation = :lastValidation WHERE id = :id", namedParameters);
    }

    public Optional<Link> findLinkByUrl(String url) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("url", url);
        return jdbcTemplate.query(
                "SELECT id, url, last_validation FROM link WHERE url = :url",
                namedParameters,
                new LinkRowMapper()
            )
            .stream()
            .findFirst();
    }
}
