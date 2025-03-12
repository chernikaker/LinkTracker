package backend.academy.scrapper.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Класс сущность для объекта ссылки */
@Getter
@Setter
@AllArgsConstructor
public class Link {

    String url;
    LinkType type;
    // время получения последнего обновления информации о ссылке
    LocalDateTime lastValidation;
}
