package backend.academy.dto;

import java.util.List;

public record ListTagsResponse(List<TagResponse> tags, long size) {}
