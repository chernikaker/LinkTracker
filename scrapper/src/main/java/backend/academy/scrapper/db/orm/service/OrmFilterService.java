package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.orm.entity.OrmFilter;
import backend.academy.scrapper.db.orm.repository.OrmFilterRepository;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;

@AllArgsConstructor
public class OrmFilterService implements FilterService {

    private final OrmFilterRepository filterRepo;

    @Override
    public List<Filter> getFiltersBySubscriptionId(long id) {
        try {
            return filterRepo.getAllBySubscription_Id(id).stream()
                    .map(this::mapFromOrmFilter)
                    .toList();
        } catch (DataAccessException e) {
            throw new ScrapperOrmException("Error while fetching filters with ORM by subscr id: " + id, e);
        }
    }

    private Filter mapFromOrmFilter(OrmFilter ormFilter) {
        return new Filter(ormFilter.key(), ormFilter.value());
    }
}
