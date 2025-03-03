package backend.academy.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Класс, описывающий все виды поддерживаемых обновлений */
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
