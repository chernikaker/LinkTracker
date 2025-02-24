package backend.academy.dto;

import java.util.List;
import backend.academy.data.Constant;
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

    /**
     * Уникальный идентификатор обновления ссылки.
     * Должен быть больше или равен 1.
     */
    @NotNull(message = "ID is required")
    @Min(value = Constant.MIN_ID, message = "ID can't be less than 1")
    private Long id;

    /**
     * URL для обновления ссылки.
     * Не должен быть пустым и должен содержать не более 2048 символов.
     */
    @NotNull(message = "URL is required")
    @NotEmpty(message = "URL cannot be empty")
    @Size(
        max = Constant.MAX_URL_LENGTH,
        message = "URL must be less than 2048 characters"
    )
    private String url;

    /**
     * Описание обновления ссылки.
     * Не должно быть пустым и должно содержать не более 500 символов.
     */
    @NotNull(message = "Description is required")
    @NotEmpty(message = "Description cannot be empty")
    @Size(
        max = Constant.MAX_DESCRIPTION_LENGTH,
        message = "Description must be less than 500 characters"
    )
    private String description;

    /**
     * Список идентификаторов чатов Telegram для уведомлений.
     * Не должен быть пустым.
     */
    @NotNull(message = "tgChatIds is required")
    @NotEmpty(message = "tgChatIds cannot be empty")
    private List<Long> tgChatIds;
}
