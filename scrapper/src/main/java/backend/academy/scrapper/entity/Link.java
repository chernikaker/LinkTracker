package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Link {

    String url;
    LinkType type;
    LocalDateTime lastValidation;

    // TODO: validation?
    public static LinkType getLinkType(String url) {
        if (url.contains("github")) {
            return LinkType.GITHUB;
        } else {
            return LinkType.STACKOVERFLOW;
        }
    }
}
