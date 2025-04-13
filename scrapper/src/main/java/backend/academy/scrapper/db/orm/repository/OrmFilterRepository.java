package backend.academy.scrapper.db.orm.repository;

import backend.academy.scrapper.db.orm.entity.OrmFilter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrmFilterRepository extends JpaRepository<OrmFilter, Long> {

    List<OrmFilter> getAllBySubscription_Id(Long subscriptionId);
}
