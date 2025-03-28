package backend.academy.scrapper.db.sql.service;

import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class SqlLinkService implements LinkService {

    private final LinkSqlRepository linkRepo;

    @Override
    @Transactional
    public Map<Long, Link> getLinksToCheck(int batchSize, long offset, long durationSeconds){
        try {
            List<SqlLink> links = linkRepo.getUncheckedLinksWithBatching(batchSize, offset, durationSeconds);
            Map<Long, Link> response = new HashMap<>();
            for (SqlLink l : links) {
                response.put(l.id(), new Link(l.url(), LinkType.fromValue(l.url()), l.lastValidation()));
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
    @Transactional
    public void updateLinkValidationOnCurrentTime(long linkId){
        updateLinkValidationWithTime(linkId, LocalDateTime.now(ZoneId.systemDefault()));
    }
}
