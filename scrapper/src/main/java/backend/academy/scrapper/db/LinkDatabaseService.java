package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Link;
import java.util.List;

public interface LinkDatabaseService {

    void updateLinkValidationNow(Link link);
    List<Link> getLinksToCheck(int batchSize, long offset, long duration);

}
