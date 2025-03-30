package backend.academy.scrapper.db.orm.mapper;

import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.entity.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrmUserMapper {

    public static OrmUser mapToOrm(User user) {
        OrmUser ormUser = new OrmUser();
        ormUser.chatId(user.chatId());
        return ormUser;
    }

    public static User mapFromOrm(OrmUser ormUser) {
        return new User(ormUser.chatId());
    }
}
