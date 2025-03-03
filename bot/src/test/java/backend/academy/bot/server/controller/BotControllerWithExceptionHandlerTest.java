package backend.academy.bot.server.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.exception.BotException;
import backend.academy.bot.server.ApplicationExceptionHandler;
import backend.academy.bot.server.BotController;
import backend.academy.bot.server.BotService;
import backend.academy.dto.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {BotController.class, ApplicationExceptionHandler.class})
@ContextConfiguration(classes = {BotController.class, ApplicationExceptionHandler.class})
public class BotControllerWithExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BotService botService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    public void processUpdates_valid() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "https://example.com", "Test message", List.of(1L, 2L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isOk());

        verify(botService).sendUpdates(linkUpdate);
    }

    @Test
    @SneakyThrows
    public void processUpdates_nullRequest() {
        LinkUpdate linkUpdate = new LinkUpdate(null, null, null, null);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Incorrect update params"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void processUpdates_emptyUrlRequest() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "", "description", List.of(1L, 2L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Incorrect update params"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void processUpdates_emptyChatsListRequest() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "url", "description", List.of());

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Incorrect update params"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void processUpdates_invalidLinkIdRequest() {
        LinkUpdate linkUpdate = new LinkUpdate(-1L, "url", "description", List.of(1L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Incorrect update params"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void processUpdates_serviceError() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "url", "description", List.of(-1L));
        doThrow(new BotException("error")).when(botService).sendUpdates(linkUpdate);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("BotException"));
    }
}
