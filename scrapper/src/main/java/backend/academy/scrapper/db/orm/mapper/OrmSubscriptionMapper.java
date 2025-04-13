package backend.academy.scrapper.db.orm.mapper;

import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrmSubscriptionMapper {

    public Subscription mapFromOrm(OrmSubscription ormSubscr) {
        List<Tag> tags = ormSubscr.tags().stream().map(OrmTagMapper::mapFromOrm).toList();
        List<Filter> filters =
                ormSubscr.filters().stream().map(OrmFilterMapper::mapFromOrm).toList();
        return new Subscription(
                OrmUserMapper.mapFromOrm(ormSubscr.user()), OrmLinkMapper.mapFromOrm(ormSubscr.link()), tags, filters);
    }
}
