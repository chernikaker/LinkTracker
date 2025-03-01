package backend.academy.scrapper.client.dto;

import java.time.LocalDateTime;

public record GithubInfo(
    String message,
    String authorName,
    LocalDateTime time,
    GithubInfoType type
) { }
