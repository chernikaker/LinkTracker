package backend.academy.scrapper.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GithubInfoType {
    COMMITS("коммит"),
    COMMENTS("комментарий"),
    ISSUES("проблема");

    private final String message;
}
