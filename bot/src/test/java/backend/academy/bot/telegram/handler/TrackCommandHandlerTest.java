package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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

public class TrackCommandHandlerTest {

    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private TrackCommandHandler trackCommandHandler;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
    }

    @Test
    public void processRequest_createNewTrackingAndReturnMessage() {
        when(message.text()).thenReturn("/track");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        String result = trackCommandHandler.processRequest(message);

        assertEquals(Constant.LINK_TRACK_MESSAGE, result);
        verify(repository).setTrack(eq(CHAT_ID), any(LinkTrackingObject.class));
    }

    @Test
    public void canHandle_shouldReturnTrue_commandIsTrackAndNoExistingTrack() {
        when(message.text()).thenReturn("/track");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        boolean result = trackCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/untrack", "text"})
    public void canHandle_shouldReturnFalse_commandIsNotTrack(String command) {
        when(message.text()).thenReturn(command);
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        boolean result = trackCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_trackAlreadyExists() {
        when(message.text()).thenReturn("/track");

        when(repository.containsTrack(CHAT_ID)).thenReturn(true);

        boolean result = trackCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
