package backend.academy.scrapper.db.sql.service;

import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.sql.entity.SqlFilter;
import backend.academy.scrapper.db.sql.repository.FilterSqlRepository;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class SqlFilterService implements FilterService {

    private final FilterSqlRepository filterRepo;

    @Override
    public List<Filter> getFiltersBySubscriptionId(long id) {
        try {
            List<SqlFilter> filters = filterRepo.getFiltersBySubscriptionId(id);
            List<Filter> response = new ArrayList<>();
            for(SqlFilter filter : filters){
                response.add(new Filter(filter.key(), filter.value()));
            }
            return response;
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Error while getting filters with sql", e);
        }
    }
}
