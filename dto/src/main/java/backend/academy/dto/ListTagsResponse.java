package backend.academy.dto;

import javax.swing.text.html.HTML;
import java.util.List;

public record ListTagsResponse(List<TagResponse> tags, long size) {
}
