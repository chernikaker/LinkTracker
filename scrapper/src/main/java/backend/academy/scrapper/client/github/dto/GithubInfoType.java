package backend.academy.scrapper.client.github.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GithubInfoType {
    COMMITS("commit"),
    COMMENTS("comment"),
    ISSUES("issue");

    private final String message;
}
