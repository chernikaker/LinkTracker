package backend.academy.dto;

import java.util.List;

public record ListTagsResponse(List<String> tags, long size) {
}
