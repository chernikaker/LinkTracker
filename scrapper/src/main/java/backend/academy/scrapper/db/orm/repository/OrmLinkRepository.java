package backend.academy.scrapper.db.orm.repository;

import backend.academy.scrapper.db.orm.entity.OrmLink;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmLinkRepository extends JpaRepository<OrmLink, Long> {

    @Query("SELECT l FROM OrmLink l WHERE l.lastValidation IS NULL OR l.lastValidation < :minValidation ORDER BY l.id")
    List<OrmLink> findUncheckedLinks(LocalDateTime minValidation, Pageable pageable);

    Optional<OrmLink> findByUrl(String url);
}
