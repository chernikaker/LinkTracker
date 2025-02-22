package backend.academy.bot.server;

import backend.academy.bot.dto.LinkUpdate;
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
    public ResponseEntity<?> processUpdates(@RequestBody LinkUpdate linkUpdate) {
        botService.sendUpdates(linkUpdate);
        System.out.println(linkUpdate);
        return ResponseEntity.ok().build();
    }
}
