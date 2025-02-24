package backend.academy.dto;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LinkUpdate {

    @NotNull(message = "ID is required")
    @Min(value = 1, message="ID can't be less than 1")
    private Long id;

    @NotNull(message = "URL is required")
    @NotEmpty(message = "URL cannot be empty")
    @Size(max = 2048, message = "URL must be less than 2048 characters")
    private String url;

    @NotNull(message = "Description is required")
    @NotEmpty(message = "Description cannot be empty")
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "tgChatIds is required")
    @NotEmpty(message = "tgChatIds cannot be empty")
    private List<Long> tgChatIds;
}
