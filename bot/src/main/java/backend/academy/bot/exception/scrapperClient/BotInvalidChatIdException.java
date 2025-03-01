package backend.academy.bot.exception.scrapperClient;

import backend.academy.bot.exception.BotException;
import backend.academy.dto.ApiErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BotInvalidChatIdException extends BotException {

    private ApiErrorResponse response;
}
