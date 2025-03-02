package backend.academy.bot.telegram.handler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.commands.TagsTextCommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TagsTextCommandHandlerTest {

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private TagsTextCommandHandler tagsTextCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_handleTagsAndUpdateState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("tag1 tag2");
        LinkTrackingObject tracking =
                new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.TRACKING_TAG);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = tagsTextCommandHandler.processRequest(message);

        assertEquals("Тэги установлены. Введите фильтры(опционально, введите '-' для пустых фильтров)", result);
        assertEquals(2, tracking.tags().length);
        assertEquals(UserState.TRACKING_FILTER, tracking.state());
    }

    @Test
    public void processRequest_shouldHandleNoTagsAndUpdateState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("-");
        LinkTrackingObject tracking =
                new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.TRACKING_TAG);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = tagsTextCommandHandler.processRequest(message);

        assertEquals("Тэги не установлены. Введите фильтры(опционально, введите '-' для пустых фильтров)", result);
        assertEquals(0, tracking.tags().length);
        assertEquals(UserState.TRACKING_FILTER, tracking.state());
    }

    @Test
    public void processRequest_noTrackInCache() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("tag1 tag2");
        when(repository.getTrack(chatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagsTextCommandHandler.processRequest(message))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void canHandle_shouldReturnTrue_stateIsTrackingTagTextIsNotCommand() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("tag1 tag2");
        LinkTrackingObject tracking =
                new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.TRACKING_TAG);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = tagsTextCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_stateIsNotTrackingTag() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("tag1 tag2");
        LinkTrackingObject tracking =
                new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.DEFAULT);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = tagsTextCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "/any"})
    public void canHandle_shouldReturnFalse_whenTextIsCommand(String command) {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(command);

        LinkTrackingObject tracking =
                new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.TRACKING_TAG);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = tagsTextCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
