package backend.academy.scrapper.entity;

import java.time.LocalDateTime;

public record Link(String url, LinkType type, LocalDateTime lastValidation) {

    // TODO: validation?
    public static LinkType getLinkType(String url) {
        if (url.contains("github")) {
            return LinkType.GITHUB;
        } else {
            return LinkType.STACKOVERFLOW;
        }
    }
}
