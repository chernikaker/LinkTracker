package backend.academy.scrapper.client.dto;

import java.time.LocalDateTime;

public record GithubCommitInfo(String message, String commiterName, LocalDateTime time) { }
