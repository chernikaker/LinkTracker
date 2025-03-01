package backend.academy.scrapper.client.dto;

import java.time.LocalDateTime;

public record UpdateInfo(
    String message,
    String authorName,
    LocalDateTime time,
    UpdateInfoType type
) { }
