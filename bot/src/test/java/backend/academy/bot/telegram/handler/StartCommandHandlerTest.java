package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.StartCommandHandler;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StartCommandHandlerTest {

    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private StartCommandHandler startCommandHandler;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);
    }

    @Test
    public void processRequest_newClient() {
        doNothing().when(service).registerNewClient(CHAT_ID);

        String result = startCommandHandler.processRequest(message);

        assertEquals(Constant.CHAT_REGISTERED, result);
        verify(service).registerNewClient(CHAT_ID);
    }

    @Test
    public void processRequest_clientAlreadyExists() {
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "Client already exists", "400", "BadRequestException", "Client already exists", List.of())))
                .when(service)
                .registerNewClient(CHAT_ID);

        String result = startCommandHandler.processRequest(message);

        assertEquals(Constant.ALREADY_REGISTERED, result);
        verify(service).registerNewClient(CHAT_ID);
    }

    @Test
    public void processRequest_registrationFails() {
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "Registration rejected", "400", "BadRequestException", "Invalid request", List.of())))
                .when(service)
                .registerNewClient(CHAT_ID);

        String result = startCommandHandler.processRequest(message);

        assertEquals(Constant.REGISTRATION_CANCELLED, result);
        verify(service).registerNewClient(CHAT_ID);
    }

    @Test
    public void canHandle_shouldReturnTrueForStartCommand() {
        when(message.text()).thenReturn("/start");

        boolean result = startCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String command) {
        when(message.text()).thenReturn(command);

        boolean result = startCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
