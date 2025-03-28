package backend.academy.scrapper.db;

import backend.academy.scrapper.entity.Filter;
import java.util.List;

public interface FilterService {

    List<Filter> getSubscriptionFiltersById(long id);

}
