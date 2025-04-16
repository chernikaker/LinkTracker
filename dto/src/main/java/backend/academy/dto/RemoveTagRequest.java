package backend.academy.dto;

import backend.academy.data.Constant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RemoveTagRequest(
    @NotNull(message = "Tag is required")
    @NotEmpty(message = "Tag cannot be empty")
    @Size(max = Constant.MAX_TAG_LENGTH, message = "Tag must be less than 50 characters")
    String tag
) { }
