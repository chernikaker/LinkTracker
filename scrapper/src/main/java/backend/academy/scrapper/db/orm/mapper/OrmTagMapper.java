package backend.academy.scrapper.db.orm.mapper;

import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.entity.Tag;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrmTagMapper {

    public OrmTag mapToOrm(Tag tag) {
        OrmTag newTag = new OrmTag();
        newTag.tagText(tag.value());
        return newTag;
    }

    public Tag mapFromOrm(OrmTag ormTag) {
        return new Tag(ormTag.tagText());
    }
}
