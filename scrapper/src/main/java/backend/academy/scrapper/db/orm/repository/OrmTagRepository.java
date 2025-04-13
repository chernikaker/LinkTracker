package backend.academy.scrapper.db.orm.repository;

import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrmTagRepository extends JpaRepository<OrmTag, Long> {

    Optional<OrmTag> findByTagTextAndOwner(String value, OrmUser u);
}
