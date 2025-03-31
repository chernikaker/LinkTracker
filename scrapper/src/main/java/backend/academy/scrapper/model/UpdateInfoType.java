package backend.academy.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Класс, описывающий все виды поддерживаемых обновлений */
@AllArgsConstructor
@Getter
public enum UpdateInfoType {
    PULL_REQUEST("pull request"),
    ISSUE("issue"),
    COMMENT("comment"),
    ANSWER("answer");

    private final String message;
}
