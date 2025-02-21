package backend.academy.bot.model;

import lombok.Getter;

@Getter
public enum UserState {

    DEFAULT(1),
    TRACKING_LINK(2),
    TRACKING_TAG(3),
    TRACKING_FILTER(4);

    private final int stateValue;

    UserState(int stateValue) {
        this.stateValue = stateValue;
    }
}
