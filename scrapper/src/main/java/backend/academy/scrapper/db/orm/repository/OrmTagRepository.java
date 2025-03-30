package backend.academy.scrapper.db.orm.repository;

import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrmTagRepository extends JpaRepository<OrmTag, Long> {

    Optional<OrmTag> findByTagTextAndOwner(String value, OrmUser u);
}
