package backend.academy.scrapper.db.orm.mapper;

import backend.academy.scrapper.db.orm.entity.OrmFilter;
import backend.academy.scrapper.entity.Filter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrmFilterMapper {

    public Filter mapFromOrm(OrmFilter ormFilter) {
        return new Filter(ormFilter.key(), ormFilter.value());
    }

    public OrmFilter mapToOrm(Filter f) {
        OrmFilter newFilter = new OrmFilter();
        newFilter.key(f.key());
        newFilter.value(f.value());
        return newFilter;
    }
}
