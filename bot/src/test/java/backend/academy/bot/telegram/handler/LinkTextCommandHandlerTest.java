package backend.academy.bot.telegram.handler;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.commands.LinkTextCommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class LinkTextCommandHandlerTest {

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private LinkTextCommandHandler linkTextCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_ValidLinkAndUpdateState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("https://github.com/owner/repo");
        LinkTrackingObject tracking = new LinkTrackingObject(null, new String[0], new String[0], UserState.TRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = linkTextCommandHandler.processRequest(message);

        assertEquals("Введите тэги(опционально, введите '-' для пустых тегов)", result);
        assertEquals("https://github.com/owner/repo", tracking.link()); // Проверяем, что ссылка обновлена
        assertEquals(UserState.TRACKING_TAG, tracking.state()); // Проверяем, что состояние обновлено
    }

    @Test
    public void processRequest_LinkIsInvalid() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("invalid-link");

        LinkTrackingObject tracking = new LinkTrackingObject(null, new String[0], new String[0], UserState.TRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = linkTextCommandHandler.processRequest(message);

        String expectedMessage = """
            Ссылка введена неверно или не поддерживается, попробуйте ещё раз.
            Подробнее о формате в /help
            """;
        assertEquals(expectedMessage, result);
        assertEquals(UserState.TRACKING_LINK, tracking.state());
    }

    @Test
    public void canHandle_shouldReturnTrue() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("https://github.com/owner/repo");
        LinkTrackingObject tracking = new LinkTrackingObject(null, new String[0], new String[0], UserState.TRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = linkTextCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_StateIsNotTrackingLink() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("https://github.com/owner/repo");
        LinkTrackingObject tracking = new LinkTrackingObject(null, new String[0], new String[0], UserState.DEFAULT);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = linkTextCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"/help","/track","/start", "/any"})
    public void canHandle_shouldReturnFalse_TextIsCommand(String command) {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(command);

        LinkTrackingObject tracking = new LinkTrackingObject(null, new String[0], new String[0], UserState.TRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = linkTextCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
