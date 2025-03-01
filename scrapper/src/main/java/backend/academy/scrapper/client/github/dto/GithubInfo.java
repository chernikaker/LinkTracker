package backend.academy.scrapper.client.github.dto;

import java.time.LocalDateTime;

public record GithubInfo(
    String message,
    String authorName,
    LocalDateTime time,
    GithubInfoType type
) { }
