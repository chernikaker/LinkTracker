package backend.academy.dto;

import java.util.List;

public record ListTagLinksResponse(String tag, List<LinkResponse> links, int size) {
}
