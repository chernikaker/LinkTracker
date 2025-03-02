package backend.academy.bot.telegram.handler;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.telegram.handler.commands.TrackCommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TrackCommandHandlerTest {

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private TrackCommandHandler trackCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_createNewTrackingAndReturnMessage() {

        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/track");
        when(repository.containsTrack(chatId)).thenReturn(false);

        String result = trackCommandHandler.processRequest(message);

        assertEquals("Введите ссылку для отслеживания", result);
        verify(repository).setTrack(eq(chatId), any(LinkTrackingObject.class));
    }

    @Test
    public void canHandle_shouldReturnTrue_commandIsTrackAndNoExistingTrack() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/track");
        when(repository.containsTrack(chatId)).thenReturn(false);

        boolean result = trackCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help","/untrack","text"})
    public void canHandle_shouldReturnFalse_commandIsNotTrack(String command) {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(command);
        when(repository.containsTrack(chatId)).thenReturn(false);

        boolean result = trackCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_trackAlreadyExists() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/track");

        when(repository.containsTrack(chatId)).thenReturn(true);

        boolean result = trackCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
