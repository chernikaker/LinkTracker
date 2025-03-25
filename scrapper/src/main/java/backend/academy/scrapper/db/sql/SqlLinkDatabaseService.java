package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.LinkDatabaseService;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@AllArgsConstructor
public class SqlLinkDatabaseService implements LinkDatabaseService {

    private final LinkSqlRepository linkRepo;

    @Override
    public List<Link> getLinksToCheck(int batchSize, long offset, long durationSeconds){
        try {
            return linkRepo.getUncheckedLinksWithBatching(batchSize, offset, durationSeconds);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting links to check", e);
        }
    }

    public void updateLinkValidationWithTime(Link link, LocalDateTime time) {
        try {
            linkRepo.updateLinkValidationById(link.id(), time);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while updating link validation", e);
        }
    }

    @Override
    public void updateLinkValidationOnCurrentTime(Link link){
        updateLinkValidationWithTime(link, LocalDateTime.now(ZoneId.systemDefault()));
    }
}
