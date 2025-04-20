package backend.academy.bot.telegram.handler;

import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.telegram.handler.commands.TagsTextHandler;
import backend.academy.bot.telegram.handler.sender.tag_text.TagCommandSenderFactory;
import backend.academy.bot.telegram.handler.sender.tag_text.TagTextSender;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TagsTextHandlerTest {

    public static final LinkTrackingObject TRACKING = new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.TRACKING_TAG, Command.TRACK);
    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private TagCommandSenderFactory factory;

    @InjectMocks
    private TagsTextHandler tagsTextHandler;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private TagTextSender sender;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
    }

    @Test
    public void processRequest_NotTerminalState_HandleTagsAndUpdateState_PresentTags() {
        when(message.text()).thenReturn("tag1 tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.TRACK);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(Constant.ENTER_FILTER.formatted(EMPTY_INPUT), result);
        assertEquals(2, TRACKING.tags().length);
        assertThat(List.of(TRACKING.tags())).containsExactlyInAnyOrder("tag1", "tag2");
        assertEquals(UserState.TRACKING_FILTER, TRACKING.state());
    }

    @Test
    public void processRequest_NotTerminalState_HandleTagsAndUpdateState_NoTags() {
        when(message.text()).thenReturn(EMPTY_INPUT);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.TRACK);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(Constant.ENTER_FILTER_NO_TAGS.formatted(EMPTY_INPUT), result);
        assertEquals(0, TRACKING.tags().length);
        assertEquals(UserState.TRACKING_FILTER, TRACKING.state());
    }

    @ParameterizedTest
    @EnumSource(value = Command.class, names = {"REMOVE_TAG_SUB","UNTRACK_BY_TAG","REMOVE_TAG", "LIST_BY_TAG"})
    public void processRequest_TerminalState_SendRequest(Command command) {
        when(message.text()).thenReturn("tag");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(command);
        when(factory.getSenderByCommand(command)).thenReturn(sender);
        when(sender.writeTagAndSendRequest("tag", TRACKING, message.chat().id())).thenReturn("answer");

        String result = tagsTextHandler.processRequest(message);

        assertEquals("answer", result);
    }



    @Test
    public void canHandle_shouldReturnTrue_stateIsTrackingTagTextIsNotCommand() {
        when(message.text()).thenReturn("tag1 tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        boolean result = tagsTextHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_stateIsNotTrackingTag() {
        when(message.text()).thenReturn("tag1 tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.state(UserState.DEFAULT);

        boolean result = tagsTextHandler.canHandle(message);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "/any"})
    public void canHandle_shouldReturnFalse_whenTextIsCommand(String command) {
        when(message.text()).thenReturn(command);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        boolean result = tagsTextHandler.canHandle(message);

        assertFalse(result);
    }
}
