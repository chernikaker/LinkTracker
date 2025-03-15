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

    private long id;
    private String url;
    private LinkType type;
    // время получения последнего обновления информации о ссылке
    private LocalDateTime lastValidation;

    public Link(String url, LinkType linkType, LocalDateTime date) {
        this(0, url, linkType, date);
    }
}
