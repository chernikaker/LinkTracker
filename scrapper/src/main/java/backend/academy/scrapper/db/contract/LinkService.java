package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.Map;

public interface LinkService {

    void updateLinkValidationOnTime(long linkId, LocalDateTime time);

    Map<Long, Link> getLinksToCheck(int batchSize, long offset, long duration);
}
