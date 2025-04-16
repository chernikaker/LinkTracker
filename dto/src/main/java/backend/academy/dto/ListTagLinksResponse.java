package backend.academy.dto;

import java.util.List;

public record ListTagLinksResponse(String tag, ListLinksResponse links) {
}
