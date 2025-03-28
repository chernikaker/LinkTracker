package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Filter;
import java.util.List;

public interface FilterService {

    List<Filter> getFiltersBySubscriptionId(long id);

}
