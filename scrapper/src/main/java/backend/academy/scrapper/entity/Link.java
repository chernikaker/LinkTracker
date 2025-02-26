package backend.academy.scrapper.entity;

public record Link(String url, LinkType type) {

    // TODO: validation?
    public static LinkType getLinkType(String url) {
        if (url.contains("github")) {
            return LinkType.GITHUB;
        } else {
            return LinkType.STACKOVERFLOW;
        }
    }
}
