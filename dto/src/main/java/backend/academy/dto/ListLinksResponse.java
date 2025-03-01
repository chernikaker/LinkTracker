package backend.academy.dto;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, int size) {}
