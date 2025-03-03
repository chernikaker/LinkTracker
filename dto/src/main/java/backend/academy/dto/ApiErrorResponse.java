package backend.academy.dto;

import java.util.List;

/**
 * DTO ответа с ошибкой
 *
 * @param description описание ошибки
 * @param code код ответа
 * @param exceptionName имя ошибки
 * @param exceptionMessage сообщение ошибки
 * @param stacktrace стектрейс ошибки
 */
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
