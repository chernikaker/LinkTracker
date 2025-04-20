package backend.academy.bot.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.exception.BotException;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
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

    public static final String URL = "https://example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BotService botService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final LocalDateTime updateTime = LocalDateTime.now(ZoneId.systemDefault());
    private final LinkUpdateUnit unit = new LinkUpdateUnit("title", "description", updateTime, "author", "type");

    @Test
    @SneakyThrows
    public void processUpdates_valid() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, URL, List.of(unit), Map.of(1L, List.of(), 2L, List.of()));

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
        LinkUpdate linkUpdate = new LinkUpdate(1L, "", List.of(unit), Map.of(1L, List.of(), 2L, List.of()));

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
    public void processUpdates_emptyChatsDataRequest() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, URL, List.of(unit), Map.of());

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
    public void processUpdates_emptyUpdatesRequest() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, URL, List.of(), Map.of(1L, List.of(), 2L, List.of()));

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
        LinkUpdate linkUpdate = new LinkUpdate(-1L, URL, List.of(unit), Map.of(1L, List.of(), 2L, List.of()));

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
        LinkUpdate linkUpdate = new LinkUpdate(1L, URL, List.of(unit), Map.of(-1L, List.of()));
        doThrow(new BotException("error")).when(botService).sendUpdates(linkUpdate);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("BotException"));
    }
}
