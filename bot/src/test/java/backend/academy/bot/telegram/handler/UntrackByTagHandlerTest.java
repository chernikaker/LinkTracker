package backend.academy.bot.telegram.handler;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.commands.UntrackByTagHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UntrackByTagHandlerTest {

    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private UntrackByTagHandler untrackByTagHandler;

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
        ArgumentCaptor<LinkTrackingObject> captor = ArgumentCaptor.forClass(LinkTrackingObject.class);

        String result = untrackByTagHandler.processRequest(message);

        assertEquals(Constant.TAG_TRACKING_MESSAGE, result);
        verify(repository).setTrack(eq(CHAT_ID), captor.capture());
        assertEquals(UserState.TRACKING_TAG, captor.getValue().state());
        assertEquals(Command.UNTRACK_BY_TAG, captor.getValue().command());
    }

    @Test
    public void canHandle_shouldReturnTrue_commandIsCorrectAndNoExistingTrack() {
        when(message.text()).thenReturn("/untrack_by_tag");
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        boolean result = untrackByTagHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "text"})
    public void canHandle_shouldReturnFalse_commandIsWrong(String command) {
        when(message.text()).thenReturn(command);
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);

        boolean result = untrackByTagHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_trackAlreadyExists() {
        when(message.text()).thenReturn("/untrack_by_tag");
        when(repository.containsTrack(CHAT_ID)).thenReturn(true);

        boolean result = untrackByTagHandler.canHandle(message);

        assertFalse(result);
    }
}
