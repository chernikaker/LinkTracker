package backend.academy.dto;

import backend.academy.data.Constant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Запрос, содержащий обновление ссылки
 *
 * @param id id ссылки
 * @param url ссылка
 * @param description описание обновления
 * @param tgChatIds id чатов для рассылки обновления
 */
public record LinkUpdate(
        @NotNull(message = "ID is required") @Min(value = Constant.MIN_ID, message = "ID can't be less than 1") Long id,
        @NotNull(message = "URL is required")
                @NotEmpty(message = "URL cannot be empty")
                @Size(max = Constant.MAX_URL_LENGTH, message = "URL must be less than 2048 characters")
                String url,
        @NotNull(message = "Description is required")
                @NotEmpty(message = "Description cannot be empty")
                @Size(max = Constant.MAX_DESCRIPTION_LENGTH, message = "Description must be less than 500 characters")
                String description,
        @NotNull(message = "tgChatIds is required") @NotEmpty(message = "tgChatIds cannot be empty")
                List<Long> tgChatIds) {}
