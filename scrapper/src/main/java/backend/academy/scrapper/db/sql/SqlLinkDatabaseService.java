package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.LinkDatabaseService;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SqlLinkDatabaseService implements LinkDatabaseService {

    private final LinkSqlRepository linkRepo;

    @Override
    public Map<Long, Link> getLinksToCheck(int batchSize, long offset, long durationSeconds){
        try {
            List<SqlLink> links = linkRepo.getUncheckedLinksWithBatching(batchSize, offset, durationSeconds);
            Map<Long, Link> response = new HashMap<>();
            for (SqlLink link : links) {
                response.put(link.id(), new Link(link.url(), LinkType.fromValue(link.url()), link.lastValidation()));
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting links to check", e);
        }
    }

    public void updateLinkValidationWithTime(long linkId, LocalDateTime time) {
        try {
            linkRepo.updateLinkValidationById(linkId, time);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while updating link validation", e);
        }
    }

    @Override
    public void updateLinkValidationOnCurrentTime(long linkId){
        updateLinkValidationWithTime(linkId, LocalDateTime.now(ZoneId.systemDefault()));
    }
}
