package backend.academy.bot.service;

import backend.academy.bot.exception.BotException;
import backend.academy.dto.ApiErrorResponse;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Глобальный обработчик ошибок для контроллера. В случае ошибки возвращается DTO, соответствующее OpenAPI контракту.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Обработка исключения валидации DTO в запросе
     *
     * @param ex исключение MethodArgumentNotValidException
     * @return обертка над объектом ApiErrorResponse для HTTP ответа
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NotNull MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        return handleIncorrectRequest(ex, status);
    }

    /**
     * Обработка исключения несоответствия типа данных из запроса ожидаемому типу.
     *
     * @param ex исключение TypeMismatchException
     * @return обертка над объектом ApiErrorResponse для HTTP ответа
     */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            @NotNull TypeMismatchException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        return handleIncorrectRequest(ex, status);
    }

    /**
     * Обработка исключения привязки запроса.
     *
     * @param ex исключение ServletRequestBindingException
     * @return обертка над объектом ApiErrorResponse для HTTP ответа
     */
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            @NotNull ServletRequestBindingException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        return handleIncorrectRequest(ex, status);
    }

    /**
     * Обработка исключения чтения тела HTTP-запроса.
     *
     * @param ex исключение HttpMessageNotReadableException
     * @return обертка над объектом ApiErrorResponse для HTTP ответа
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NotNull HttpMessageNotReadableException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        return handleIncorrectRequest(ex, status);
    }

    /**
     * Метод формирует ApiErrorResponse из стандартного исключения
     *
     * @param ex возникшее исключение
     * @param status статус ответа
     * @return обертка над объектом ApiErrorResponse для HTTP ответа
     */
    private ResponseEntity<Object> handleIncorrectRequest(Exception ex, HttpStatusCode status) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        "Incorrect update params",
                        String.valueOf(status.value()),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()),
                status);
    }

    /**
     * Обработка кастомного исключения приложения
     *
     * @param ex исключение Bot
     * @return обертка над объектом ApiErrorResponse для HTTP ответа
     */
    @ExceptionHandler(BotException.class)
    public ResponseEntity<ApiErrorResponse> handleScrapperException(BotException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        BotException.DESCRIPTION,
                        String.valueOf(ex.getStatus()),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()),
                HttpStatusCode.valueOf(ex.getStatus()));
    }
}
