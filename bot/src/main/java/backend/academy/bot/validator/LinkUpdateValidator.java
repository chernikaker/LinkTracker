package backend.academy.bot.validator;


import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.exception.custom.BotIllegalRequestArgumentException;
import backend.academy.bot.exception.custom.BotValidationException;
import org.springframework.stereotype.Component;

@Component
public class LinkUpdateValidator {

    public void validate(LinkUpdate link) {
        if (link == null) {
            throw new BotValidationException("Link object in body is null");
        }
        for (Long chatId : link.tgChatIds()) {
            if (chatId == null) {
                throw new BotIllegalRequestArgumentException("Chat id is null in link update "+ link.id());
            }
            if (chatId < 1) {
                throw new BotIllegalRequestArgumentException("Chat id is less than 1 in link update "+ link.id());
            }
        }
    }
}
