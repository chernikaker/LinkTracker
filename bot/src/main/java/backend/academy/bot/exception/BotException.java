package backend.academy.bot.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BotException extends RuntimeException {

    private final String description = "Internal Bot App Exception";

    public int getStatus() {
        return 400;
    }

    public BotException(String message) {
        super(message);
    }
}
