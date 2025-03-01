package backend.academy.scrapper.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UpdateInfoType {
    COMMIT("commit"),
    COMMENT("comment"),
    ISSUE("issue"),
    ANSWER("answer"),
    ;

    private final String message;
}
