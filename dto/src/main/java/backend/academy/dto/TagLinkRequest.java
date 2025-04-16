package backend.academy.dto;

import backend.academy.data.Constant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TagLinkRequest(
    @NotNull(message = "URL is required")
    @NotEmpty(message = "URL cannot be empty")
    @Size(max = Constant.MAX_URL_LENGTH, message = "URL must be less than 2048 characters")
    String link,
    @NotNull(message = "Tags cannot be null")
    @NotEmpty(message = "Tags cannot be empty")
    List<String> tags) { }
