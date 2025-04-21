package backend.academy.bot.telegram.handler.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.EXTERNAL_ERROR;
import static backend.academy.bot.telegram.handler.Constant.NOT_REGISTERED;
import static backend.academy.bot.telegram.handler.Constant.NO_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_USER;
import static backend.academy.bot.telegram.handler.Constant.UNKNOWN_ERROR;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.dto.ApiErrorResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Общий класс обработки и посылки сообщения на этапе ввода тегов
 * @see backend.academy.bot.telegram.handler.commands.TagsTextHandler
 */
@AllArgsConstructor
public abstract class TagTextSender {

    protected final InMemoryTrackingCache repository;
    protected final ScrapperClientService service;

    public abstract String writeTagAndSendRequest(String tagLine, LinkTrackingObject tracking, long id);

    /**
     * Разбор ошибки и отправка сообщения о ней
     * @param response DTO ошибки
     * @param tagForUser флаг проверки наличия тега для пользователя или подписки
     * @return сообщение об ошибке для пользователя
     */
    protected String getErrorMessage(ApiErrorResponse response, boolean tagForUser) {
        if (response == null) {
            return UNKNOWN_ERROR;
        }
        if (response.code().equals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))) {
            return EXTERNAL_ERROR;
        }
        if (response.exceptionName().contains("UserNotExist")) {
            return NOT_REGISTERED;
        }
        if (response.exceptionName().contains("SubscriptionNotExist")) {
            return NO_SUBSCRIPTION;
        }
        if (response.code().equals(String.valueOf(HttpStatus.NOT_FOUND.value()))
                || response.exceptionName().contains("TagNotExist")) {
            return tagForUser ? NO_TAG_FOR_USER : NO_TAG_FOR_SUBSCRIPTION;
        }
        return UNKNOWN_ERROR;
    }
}
