package backend.academy.bot.service;

import backend.academy.dto.LinkUpdate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BotController {

    private final BotService botService;

    /**
     * Метод принимает запросы с обновлениями ссылок для пользователей
     *
     * @param linkUpdate DTO с обновлениями
     * @return пустой ответ с кодом 200 в случае успешной рассылки. DTO ошибки с кодом 400 в случае исключения
     * @see ApplicationExceptionHandler
     */
    @PostMapping("/updates")
    public ResponseEntity<?> processUpdates(@RequestBody @Valid LinkUpdate linkUpdate) {
        botService.sendUpdates(linkUpdate);
        return ResponseEntity.ok().build();
    }
}
