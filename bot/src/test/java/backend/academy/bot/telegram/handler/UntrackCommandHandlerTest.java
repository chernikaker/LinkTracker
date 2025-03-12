package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.telegram.handler.commands.UntrackCommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UntrackCommandHandlerTest {

    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private UntrackCommandHandler untrackCommandHandler;

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
        when(message.text()).thenReturn("/untrack");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        String result = untrackCommandHandler.processRequest(message);

        assertEquals(Constant.LINK_UNTRACK_TEXT, result);
        verify(repository).setTrack(eq(CHAT_ID), any(LinkTrackingObject.class));
    }

    @Test
    public void canHandle_shouldReturnTrue_commandIsUntrackAndNoExistingTrack() {
        when(message.text()).thenReturn("/untrack");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        boolean result = untrackCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "text"})
    public void canHandle_shouldReturnFalse_commandIsNotUntrack() {
        when(message.text()).thenReturn("/start");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        boolean result = untrackCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_trackAlreadyExists() {
        when(message.text()).thenReturn("/untrack");
        when(repository.containsTrack(CHAT_ID)).thenReturn(true);

        boolean result = untrackCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
