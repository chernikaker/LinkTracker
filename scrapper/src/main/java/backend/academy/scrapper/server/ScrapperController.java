package backend.academy.scrapper.server;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.ListLinksResponse;
import backend.academy.scrapper.exception.custom.controller.ScrapperControllerEntityNotFoundException;
import backend.academy.scrapper.exception.custom.controller.ScrapperInvalidIdException;
import backend.academy.scrapper.exception.custom.repository.ScrapperUserNotExistsException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
        try {
            scrapperService.deleteUser(id);
        } catch (ScrapperUserNotExistsException ex) {
            throw new ScrapperControllerEntityNotFoundException(ex, ex.getMessage());
        }
        return ResponseEntity.ok("Chat deleted successfully");
    }

    @GetMapping("/links")
    public final ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        ListLinksResponse response = scrapperService.getUserLinks(id);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @PostMapping("/links")
    public final ResponseEntity<?> addLinkSubscription(
        @RequestHeader("Tg-Chat-Id") final Long id,
        @RequestBody @Valid final AddLinkRequest request
    ) {
        LinkResponse response = scrapperService.addSubscription(id, request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

}
