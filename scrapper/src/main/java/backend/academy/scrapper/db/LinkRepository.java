package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository {

    Long addLink(Link link);

    List<Link> getLinksWithBatching(int batchSize, long offset);

    void removeLinkById(long linkId);

    void updateLinkValidation(Link link, LocalDateTime lastValidation);
}
