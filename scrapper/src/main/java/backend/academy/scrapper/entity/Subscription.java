package backend.academy.scrapper.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Сущность подписки пользователя на ссылку */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

    private User user;
    private Link link;
    private List<Tag> tags;
    private List<Filter> filters;
}
