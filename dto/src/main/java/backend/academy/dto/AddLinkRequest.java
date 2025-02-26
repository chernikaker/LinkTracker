package backend.academy.dto;

import backend.academy.data.Constant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AddLinkRequest {

    @NotNull(message = "URL is required")
    @NotEmpty(message = "URL cannot be empty")
    @Size(
        max = Constant.MAX_URL_LENGTH,
        message = "URL must be less than 2048 characters"
    )
    private String url;
    private List<String> tags;
    private List<String> filters;
}
