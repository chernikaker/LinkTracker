package backend.academy.scrapper.server;

import backend.academy.scrapper.exception.custom.ScrapperInvalidIdException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ScrapperController {

    private final ScrapperService scrapperService;
    /**
     * Регистрирует новый Telegram чат.<br>
     *
     * @param id идентификатор Telegram чата,
     * который необходимо зарегистрировать.
     * @return ResponseEntity с сообщением об успешной регистрации.
     */
    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<?> registerChat(@PathVariable final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        scrapperService.registerUser(id);
        return ResponseEntity.ok("Chat registered successfully");
    }

    /**
     * Удаляет существующий Telegram чат.
     * @param id идентификатор Telegram чата, который необходимо удалить.
     * @return ResponseEntity с сообщением об успешном удалении.
     */
    @DeleteMapping("/tg-chat/{id}")
    public final ResponseEntity<?> deleteChat(@PathVariable final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        scrapperService.deleteUser(id);
        return ResponseEntity.ok("Chat deleted successfully");
    }

}
