package backend.academy.bot.validator;


import backend.academy.bot.dto.LinkUpdate;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class LinkUpdateValidator {

    public void validate(LinkUpdate link) {
        if (link == null) {
            throw new ValidationException("Link object is null");
        }
        for (Long chatId : link.tgChatIds()) {
            if (chatId == null) {
                throw new IllegalArgumentException("Chat id is null in link update "+ link.id());
            }
            if (chatId < 1) {
                throw new IllegalArgumentException("Chat id is less than 1 in link update "+ link.id());
            }
        }
    }
}
