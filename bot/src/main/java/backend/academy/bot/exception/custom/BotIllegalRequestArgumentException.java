package backend.academy.bot.exception.custom;

public class BotIllegalRequestArgumentException extends  BotException{

    public BotIllegalRequestArgumentException(String message) {
        super(message);
    }
}
