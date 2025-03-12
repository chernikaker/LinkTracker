package backend.academy.bot.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
public class BotException extends RuntimeException {

    public static final String DESCRIPTION = "Internal Bot App Exception";

    public int getStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }

    public BotException(String message) {
        super(message);
    }
}
