package backend.academy.bot.telegram.handler.sender.tag_text;

import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import static backend.academy.bot.telegram.handler.Constant.INTERNAL_ERROR;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.NO_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_USER;
import static backend.academy.bot.telegram.handler.Constant.UNKNOWN_ERROR;

public interface TagTextSender {

    String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id);

    default String getErrorMessage(ApiErrorResponse response, boolean tagForUser) {
        if(response == null) {
            return UNKNOWN_ERROR;
        }
        if (response.code().equals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))) {
            return INTERNAL_ERROR;
        }
        if(response.exceptionName().contains("UserNotExist")) {
            return NOT_REGISTERED;
        }
        if(response.exceptionName().contains("SubscriptionNotExist")) {
            return NO_SUBSCRIPTION;
        }
        if(response.code().equals(String.valueOf(HttpStatus.NOT_FOUND.value()))
            || response.exceptionName().contains("TagNotExist")) {
            return tagForUser ? NO_TAG_FOR_USER : NO_TAG_FOR_SUBSCRIPTION;
        }
        return UNKNOWN_ERROR;
    }
}
