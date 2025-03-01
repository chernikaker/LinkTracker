package backend.academy.scrapper.entity;

import backend.academy.scrapper.exception.service.ScrapperUnsupportedLinkTypeException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Link {

    String url;
    LinkType type;
    LocalDateTime lastValidation;

    public static LinkType getLinkType(String url) {
        if (url.startsWith("https://stackoverflow.com/") || url.startsWith("http://stackoverflow.com/")) {
            return LinkType.STACKOVERFLOW;
        } else if (url.startsWith("https://github.com/") || url.startsWith("http://github.com/")) {
            return LinkType.GITHUB;
        }
        throw new ScrapperUnsupportedLinkTypeException("Link type not supported: " + url);
    }
}
