package backend.academy.bot.exception;

import backend.academy.bot.exception.custom.BotException;
import backend.academy.dto.ApiErrorResponse;
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
import java.util.Arrays;

@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        @NotNull MethodArgumentNotValidException ex,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode status,
        @NotNull WebRequest request
    ) {
        return handleIncorrectRequest(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
        @NotNull TypeMismatchException ex,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode status,
        @NotNull WebRequest request
    ) {
        return handleIncorrectRequest(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
        @NotNull ServletRequestBindingException ex,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode status,
        @NotNull WebRequest request
    ) {
        return handleIncorrectRequest(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        @NotNull HttpMessageNotReadableException ex,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode status,
        @NotNull WebRequest request
    ) {
        return handleIncorrectRequest(ex, status);
    }

    private ResponseEntity<Object> handleIncorrectRequest(
        Exception ex,
        HttpStatusCode status
    ) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "Incorrect update params",
                String.valueOf(status.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toList()
            ),
            status
        );
    }

    @ExceptionHandler(BotException.class)
    public ResponseEntity<ApiErrorResponse> handleScrapperException(BotException ex) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                ex.description(),
                String.valueOf(ex.getStatus()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toList()
            ),
            HttpStatusCode.valueOf(ex.getStatus())
        );
    }
}
