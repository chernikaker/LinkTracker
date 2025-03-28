package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrmLinkService implements LinkService {

    private final OrmLinkRepository linkRepo;

    @Override
    @Transactional
    public void updateLinkValidationOnCurrentTime(long linkId) {
        try {
            OrmLink ormLink = linkRepo.findById(linkId).orElseThrow(
                () -> new ScrapperLinkNotExistsException("Link " + linkId + " not found")
            );
            ormLink.lastValidation(LocalDateTime.now(ZoneId.systemDefault()));
            linkRepo.save(ormLink);
        } catch(DataAccessException e){
            throw new ScrapperOrmException("Error while updating link validation with ORM", e);
        }
    }

    @Override
    public Map<Long, Link> getLinksToCheck(int batchSize, long offset, long duration) {
        try{
            Instant minCheck = Instant.now().minusSeconds(duration);
            Pageable pageReq = PageRequest.of((int)offset/batchSize, batchSize);
            List<OrmLink> linkData = linkRepo.findUncheckedLinks(minCheck, pageReq);
            Map<Long, Link> links = new HashMap<>();
            for (OrmLink link : linkData) {
                links.put(link.id(), new Link(link.url(), LinkType.fromValue(link.url()), link.lastValidation()));
            }
            return links;
        } catch (DataAccessException e){
            throw new ScrapperOrmException("Error while retrieving links from ORM", e);
        }
    }
}
