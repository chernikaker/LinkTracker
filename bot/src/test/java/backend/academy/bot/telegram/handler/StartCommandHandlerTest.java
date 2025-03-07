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

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private StartCommandHandler startCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_newClient() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(repository.containsTrack(chatId)).thenReturn(false);
        doNothing().when(service).registerNewClient(chatId);

        String result = startCommandHandler.processRequest(message);

        assertEquals(Constant.CHAT_REGISTERED, result);
        verify(service).registerNewClient(chatId);
    }

    @Test
    public void processRequest_clientAlreadyExists() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(repository.containsTrack(chatId)).thenReturn(false);
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "Client already exists", "400", "BadRequestException", "Client already exists", List.of())))
                .when(service)
                .registerNewClient(chatId);

        String result = startCommandHandler.processRequest(message);

        assertEquals(Constant.ALREADY_REGISTERED, result);
        verify(service).registerNewClient(chatId);
    }

    @Test
    public void processRequest_registrationFails() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(repository.containsTrack(chatId)).thenReturn(false);
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "Registration rejected", "400", "BadRequestException", "Invalid request", List.of())))
                .when(service)
                .registerNewClient(chatId);

        String result = startCommandHandler.processRequest(message);

        assertEquals(Constant.REGISTRATION_CANCELLED, result);
        verify(service).registerNewClient(chatId);
    }

    @Test
    public void canHandle_shouldReturnTrueForStartCommand() {
        Message message = mock(Message.class);
        when(message.text()).thenReturn("/start");

        boolean result = startCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String command) {
        Message message = mock(Message.class);
        when(message.text()).thenReturn(command);

        boolean result = startCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
