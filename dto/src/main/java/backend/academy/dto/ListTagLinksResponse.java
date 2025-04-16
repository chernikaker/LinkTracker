package backend.academy.dto;

import java.util.List;

public record ListTagLinksResponse(TagResponse tag, ListLinksResponse links) {
}
