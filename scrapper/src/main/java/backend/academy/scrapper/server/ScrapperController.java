package backend.academy.scrapper.server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapperController {

    /**
     * Регистрирует новый Telegram чат.<br>
     *
     * @param id идентификатор Telegram чата,
     * который необходимо зарегистрировать.
     * @return ResponseEntity с сообщением об успешной регистрации.
     */
    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<?> registerChat(@PathVariable final String id) {
        System.out.println(id);
        return ResponseEntity.ok("Chat registered successfully");
    }

    /**
     * Удаляет существующий Telegram чат.
     * @param id идентификатор Telegram чата, который необходимо удалить.
     * @return ResponseEntity с сообщением об успешном удалении.
     */
    @DeleteMapping("/tg-chat/{id}")
    public final ResponseEntity<?> deleteChat(@PathVariable final String id) {
        System.out.println(id);
        return ResponseEntity.ok("Chat deleted successfully");
    }

}
