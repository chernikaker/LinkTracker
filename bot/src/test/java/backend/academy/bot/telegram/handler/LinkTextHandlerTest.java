package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.LinkTextHandler;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LinkTextHandlerTest {

    public static final LinkTrackingObject TRACKING = new LinkTrackingObject(null, new String[0], new String[0], UserState.TRACKING_LINK, Command.TRACK);
    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private LinkTextHandler linkTextHandler;

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

    @ParameterizedTest
    @EnumSource(value = Command.class, names = {"TRACK", "TAGS_TO_SUB"})
    public void processRequest_ValidLinkNotTerminalStateNextWriteTags(Command command) {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(command);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.TAGS_TRACKING_MESSAGE.formatted(Constant.EMPTY_INPUT), result);
        assertEquals("https://github.com/owner/repo", TRACKING.link());
        assertEquals(UserState.TRACKING_TAG, TRACKING.state());
    }

    @ParameterizedTest
    @EnumSource(value = Command.class, names = {"REMOVE_TAG_SUB"})
    public void processRequest_ValidLinkNotTerminalStateNextWriteOneTag(Command command) {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(command);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.TAG_TRACKING_MESSAGE, result);
        assertEquals("https://github.com/owner/repo", TRACKING.link());
        assertEquals(UserState.TRACKING_TAG, TRACKING.state());
    }

    @Test
    public void processRequest_LinkIsInvalid() {
        when(message.text()).thenReturn("invalid-link");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.LINK_NOT_VALID, result);
        assertEquals(UserState.TRACKING_LINK, TRACKING.state());
    }

    @Test
    public void processRequest_processAndSendUntrackingRequest_Success() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.UNTRACK);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.LINK_REMOVED_SUCCESS, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).removeLinkSubscription(CHAT_ID, message.text());
    }

    @Test
    public void processRequest_processAndSendUntrackingRequest_UnknownError() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        doThrow(new BotRequestException(null)).when(service).removeLinkSubscription(eq(CHAT_ID), any());
        TRACKING.command(Command.UNTRACK);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.UNKNOWN_ERROR, result);
        verify(repository).removeTrack(CHAT_ID);
    }

    @Test
    public void processRequest_processAndSendUntrackingRequest_InternalError() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        ApiErrorResponse response = new ApiErrorResponse("","500","","", List.of());
        doThrow(new BotRequestException(response)).when(service).removeLinkSubscription(eq(CHAT_ID), any());
        TRACKING.command(Command.UNTRACK);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.EXTERNAL_ERROR, result);
        verify(repository).removeTrack(CHAT_ID);
    }

    @Test
    public void processRequest_processAndSendUntrackingRequest_UserNotRegistered() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        ApiErrorResponse response = new ApiErrorResponse("","400","ScrapperUserNotExistsException","", List.of());
        doThrow(new BotRequestException(response)).when(service).removeLinkSubscription(eq(CHAT_ID), any());
        TRACKING.command(Command.UNTRACK);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.NOT_REGISTERED, result);
        verify(repository).removeTrack(CHAT_ID);
    }

    @Test
    public void processRequest_processAndSendUntrackingRequest_NoSuchSubscription() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        ApiErrorResponse response = new ApiErrorResponse("","404","","", List.of());
        doThrow(new BotRequestException(response)).when(service).removeLinkSubscription(eq(CHAT_ID), any());
        TRACKING.command(Command.UNTRACK);

        String result = linkTextHandler.processRequest(message);

        assertEquals(Constant.NO_SUBSCRIPTION, result);
        verify(repository).removeTrack(CHAT_ID);
    }


    @Test
    public void canHandle_shouldReturnTrue() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        boolean result = linkTextHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_StateIsNotTrackingLink() {
        when(message.text()).thenReturn("https://github.com/owner/repo");
        TRACKING.state(UserState.DEFAULT);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        boolean result = linkTextHandler.canHandle(message);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "/any"})
    public void canHandle_shouldReturnFalse_TextIsCommand(String command) {
        when(message.text()).thenReturn(command);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        boolean result = linkTextHandler.canHandle(message);

        assertFalse(result);
    }
}
