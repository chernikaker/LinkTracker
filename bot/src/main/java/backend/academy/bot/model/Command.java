package backend.academy.bot.model;

public enum Command {

    //todo: add list command
    START,
    TRACK,
    UNTRACK,
    TEXT,
    HELP;

    public static Command fromString(String command) {
        return switch (command.trim().toLowerCase()) {
            case "/start" -> START;
            case "/track" -> TRACK;
            case "/untrack" -> UNTRACK;
            case "/help" -> HELP;
            default -> TEXT;
        };
    }
}
