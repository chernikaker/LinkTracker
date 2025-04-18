package backend.academy.bot.exception.handler;

import backend.academy.bot.exception.BotException;

public class BotIllegalCommandException extends BotException {

    public BotIllegalCommandException(String message) {
        super(message);
    }
}
