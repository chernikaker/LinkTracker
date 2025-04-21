package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.mapper.OrmLinkMapper;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.entity.Link;
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

/** ORM реализация сервиса работы с ссылками */
@AllArgsConstructor
public class OrmLinkService implements LinkService {

    private final OrmLinkRepository linkRepo;

    @Override
    @Transactional
    public void updateLinkValidationOnTime(long linkId, LocalDateTime dt) {
        try {
            OrmLink ormLink = linkRepo.findById(linkId)
                    .orElseThrow(() -> new ScrapperLinkNotExistsException("Link " + linkId + " not found"));
            ormLink.lastValidation(dt);
            linkRepo.save(ormLink);
            linkRepo.flush();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while updating link validation with ORM", e);
        }
    }

    @Override
    @Transactional
    public Map<Long, Link> getLinksToCheck(int batchSize, long offset, long duration) {
        try {
            LocalDateTime minCheck = Instant.now()
                    .minusSeconds(duration)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            Pageable pageReq = PageRequest.of((int) offset / batchSize, batchSize);
            List<OrmLink> linkData = linkRepo.findUncheckedLinks(minCheck, pageReq);
            Map<Long, Link> links = new HashMap<>();
            for (OrmLink link : linkData) {
                links.put(link.id(), OrmLinkMapper.mapFromOrm(link));
            }
            return links;
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while retrieving links from ORM", e);
        }
    }
}
