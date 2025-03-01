package backend.academy.bot.exception.validator;

import backend.academy.bot.exception.BotException;

public class BotValidationException extends BotException {

    public BotValidationException(String message) {
        super(message);
    }
}
