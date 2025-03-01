package backend.academy.bot.exception.validator;

import backend.academy.bot.exception.BotException;

public class BotIllegalRequestArgumentException extends BotException {

    public BotIllegalRequestArgumentException(String message) {
        super(message);
    }
}
