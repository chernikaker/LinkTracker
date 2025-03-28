package backend.academy.scrapper.db.orm.repository;

import backend.academy.scrapper.db.orm.entity.OrmFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrmFilterRepository extends JpaRepository<OrmFilter, Long> {

    List<OrmFilter> getAllBySubscription_Id(Long subscriptionId);
}
