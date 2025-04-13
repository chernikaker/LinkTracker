package backend.academy.scrapper.db.orm.mapper;

import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrmLinkMapper {

    public static OrmLink mapToOrm(Link link) {
        OrmLink ormLink = new OrmLink();
        ormLink.url(link.url());
        ormLink.lastValidation(link.lastValidation());
        return ormLink;
    }

    public static Link mapFromOrm(OrmLink ormLink) {
        return new Link(ormLink.url(), LinkType.fromValue(ormLink.url()), ormLink.lastValidation());
    }
}
