package backend.academy.bot.validator;

import backend.academy.bot.exception.validator.BotIllegalRequestArgumentException;
import backend.academy.bot.exception.validator.BotValidationException;
import backend.academy.dto.LinkUpdate;
import lombok.experimental.UtilityClass;

/** Валидатор для DTO с обновлением ссылки */
@UtilityClass
public class LinkUpdateValidator {

    public void validate(LinkUpdate link) {
        if (link == null) {
            throw new BotValidationException("Link object in body is null");
        }
        // проверяет ID чатов для рассылки обновления
        for (Long chatId : link.tgChatData().keySet()) {
            if (chatId == null) {
                throw new BotIllegalRequestArgumentException("Chat id is null in link update " + link.id());
            }
            if (chatId < 1) {
                throw new BotIllegalRequestArgumentException("Chat id is less than 1 in link update " + link.id());
            }
        }
    }
}
