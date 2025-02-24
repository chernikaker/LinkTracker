package backend.academy.bot.exception.custom;

import lombok.Getter;

@Getter
public class BotException extends RuntimeException {

    private final String description = "Internal Bot App Exception";

    public int getStatus() {
        return 400;
    }

    public BotException(String message) {
        super(message);
    }
}
