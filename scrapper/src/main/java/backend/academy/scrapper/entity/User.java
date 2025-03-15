package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

/**
 * сущность пользователя для хранения в репозитории
 */
@AllArgsConstructor
@Setter
@Getter
public class User {
    private final long id;
    private final long chatId;

    public User(long chatId) {
        this(0, chatId);
    }
}
