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

    private String url;
    private LinkType type;
    // время получения последнего обновления информации о ссылке
    private LocalDateTime lastValidation;

    public Link(String url, LinkType type) {
        this(url, type, null);
    }
}
