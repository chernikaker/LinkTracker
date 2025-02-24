package backend.academy.bot.server;

import backend.academy.bot.dto.LinkUpdate;
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

    @PostMapping(value = "/updates", consumes = {"application/json"})
    public ResponseEntity<?> processUpdates(@RequestBody(required = false) @Valid LinkUpdate linkUpdate) {
        botService.sendUpdates(linkUpdate);
        return ResponseEntity.ok("Request processed successfully");
    }
}
