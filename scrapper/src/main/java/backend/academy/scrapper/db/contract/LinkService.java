package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Link;
import java.util.Map;

public interface LinkService {

    void updateLinkValidationOnCurrentTime(long linkId);

    Map<Long, Link> getLinksToCheck(int batchSize, long offset, long duration);
}
