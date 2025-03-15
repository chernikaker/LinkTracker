package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.LinkRepository;
import backend.academy.scrapper.db.sql.mapper.LinkRowMapper;
import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LinkSqlRepository implements LinkRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LinkSqlRepository(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public List<Link> getLinksWithBatching(int batchSize, long offset){
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("batch", batchSize);
        namedParameters.addValue("offset", offset);
        return jdbcTemplate.query(
            "SELECT * FROM link LIMIT :batch OFFSET :offset",
            namedParameters,
            new LinkRowMapper()
        );
    }

    @Override
    @Transactional
    public void removeLinkById(long id) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update("DELETE FROM link WHERE id = :id", namedParameters);
    }

    @Override
    @Transactional
    public Long addLink(Link link) {
        Optional<Link> existingLinkId = findLinkByUrl(link.url());
        if (existingLinkId.isPresent()) {
            return existingLinkId.get().id();
        }
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("url", link.url());
        String addRequest = "INSERT INTO link (url, last_validation) VALUES (:url, :lastValidation) RETURNING id";
        namedParameters.addValue("lastValidation", link.lastValidation());
        return jdbcTemplate.queryForObject(addRequest, namedParameters, Long.class);
    }

    @Override
    @Transactional
    public void updateLinkValidation(Link link, LocalDateTime lastValidation) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", link.id());
        namedParameters.addValue("lastValidation", lastValidation);
        jdbcTemplate.update("UPDATE link SET last_validation = :lastValidation WHERE id = :id", namedParameters);
    }

    private Optional<Link> findLinkByUrl(String url) {
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
