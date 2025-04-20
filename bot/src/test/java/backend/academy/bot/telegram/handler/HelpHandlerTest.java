package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.telegram.handler.commands.HelpHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HelpHandlerTest {

    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private HelpHandler helpHandler;

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
    public void processRequest_ReturnHelpMessage() {
        when(message.text()).thenReturn("/help");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        String result = helpHandler.processRequest(message);

        assertEquals(Constant.HELP_MESSAGE, result);
        verify(repository, never()).removeTrack(CHAT_ID);
    }

    @Test
    public void processRequest_shouldRemoveTrackAndReturnHelpMessage() {
        when(message.text()).thenReturn("/help");
        when(repository.containsTrack(CHAT_ID)).thenReturn(true);

        String result = helpHandler.processRequest(message);

        assertEquals(Constant.HELP_MESSAGE, result);
        verify(repository).removeTrack(CHAT_ID);
    }

    @Test
    public void canHandle_shouldReturnTrueForHelpCommand() {
        when(message.text()).thenReturn("/help");

        boolean result = helpHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/start", "/track", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String command) {
        when(message.text()).thenReturn(command);

        boolean result = helpHandler.canHandle(message);

        assertFalse(result);
    }
}
