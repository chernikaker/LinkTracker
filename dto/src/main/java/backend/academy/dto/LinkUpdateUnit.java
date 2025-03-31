package backend.academy.dto;

import backend.academy.data.Constant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record LinkUpdateUnit(

    @NotNull(message = "Title is required")
    @NotEmpty(message = "Title cannot be empty")
    @Size(max = Constant.MAX_TITLE_LENGTH, message = "Title must be less than"+Constant.MAX_TITLE_LENGTH+" characters")
    String title,
    @NotNull(message = "Description is required")
    @Size(max = Constant.MAX_DESCRIPTION_LENGTH, message = "Description must be less than"+Constant.MAX_DESCRIPTION_LENGTH+" characters")
    String description,
    @NotNull(message = "Creation date is required")
    LocalDateTime creationDate,
    @NotNull(message = "Author is required")
    @NotEmpty(message = "Author cannot be empty")
    @Size(max = Constant.MAX_AUTHOR_LENGTH, message = "Author name must be less than"+Constant.MAX_AUTHOR_LENGTH+" characters")
    String author
) { }
