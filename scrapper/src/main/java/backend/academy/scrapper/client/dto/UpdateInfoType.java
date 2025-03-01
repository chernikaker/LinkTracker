package backend.academy.scrapper.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UpdateInfoType {
    COMMITS("commit"),
    COMMENTS("comment"),
    ISSUES("issue");

    private final String message;
}
