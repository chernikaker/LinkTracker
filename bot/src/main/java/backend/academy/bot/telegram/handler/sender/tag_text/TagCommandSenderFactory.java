package backend.academy.bot.telegram.handler.sender.tag_text;

import backend.academy.bot.exception.handler.BotIllegalCommandException;
import backend.academy.bot.telegram.handler.Command;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TagCommandSenderFactory {

    private final AddTagsToSubSender addTagsToSubSender;
    private final DeleteTagSender deleteTagSender;
    private final GetTagSubsSender getTagSubsSender;
    private final RemoveTagFromSubSender removeTagFromSubSender;
    private final UntrackByTagSender untrackByTagSender;

    public TagTextSender getSenderByCommand(Command command) {
        return switch (command) {
            case TAGS_TO_SUB -> addTagsToSubSender;
            case REMOVE_TAG -> deleteTagSender;
            case LIST_BY_TAG -> getTagSubsSender;
            case UNTRACK_BY_TAG -> untrackByTagSender;
            case REMOVE_TAG_SUB -> removeTagFromSubSender;
            default -> throw new BotIllegalCommandException("Non terminal command");
        };
    }
}
