package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.TagListHandler;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.TagResponse;
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

public class TagListHandlerTest {

    private static final long CHAT_ID = 123L;

    private static final String EXPECTED = """
    Теги:
    #tag1
    #tag2
    """;

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private TagListHandler tagListHandler;

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
    public void processRequest_SuccessTagsPresent() {
        ListTagsResponse tags =
                new ListTagsResponse(List.of(new TagResponse(1L, "tag1"), new TagResponse(2L, "tag2")), 2);
        when(service.getUserTags(CHAT_ID)).thenReturn(tags);

        String result = tagListHandler.processRequest(message);

        assertEquals(EXPECTED, result);
    }

    @Test
    public void processRequest_SuccessNoTagsPresent() {
        ListTagsResponse tags = new ListTagsResponse(List.of(), 0);
        when(service.getUserTags(CHAT_ID)).thenReturn(tags);

        String result = tagListHandler.processRequest(message);

        assertEquals(Constant.NO_TAGS, result);
        verify(service).getUserTags(CHAT_ID);
    }

    @Test
    public void processRequest_UserNotRegistered() {
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);
        ApiErrorResponse response = new ApiErrorResponse("", "400", "UserNotExist", "", List.of());
        when(service.getUserTags(CHAT_ID)).thenThrow(new BotRequestException(response));

        String result = tagListHandler.processRequest(message);

        assertEquals(Constant.NOT_REGISTERED, result);
    }

    @Test
    public void processRequest_InternalException() {
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);
        ApiErrorResponse response = new ApiErrorResponse("", "500", "", "", List.of());
        when(service.getUserTags(CHAT_ID)).thenThrow(new BotRequestException(response));

        String result = tagListHandler.processRequest(message);

        assertEquals(Constant.EXTERNAL_ERROR, result);
    }

    @Test
    public void processRequest_UnknownException() {
        when(repository.containsTrack(CHAT_ID)).thenReturn(false);
        when(service.getUserTags(CHAT_ID)).thenThrow(new BotRequestException(null));

        String result = tagListHandler.processRequest(message);

        assertEquals(Constant.UNKNOWN_ERROR, result);
    }

    @Test
    public void canHandle_shouldReturnTrueForTagsCommand() {
        when(message.text()).thenReturn(Command.TAGS.command());
        when(repository.containsTrack(anyLong())).thenReturn(false);

        boolean result = tagListHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String request) {
        when(message.text()).thenReturn(request);
        when(repository.containsTrack(anyLong())).thenReturn(false);

        boolean result = tagListHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_whenUserIsTracked() {
        when(message.text()).thenReturn(Command.TAGS.command());
        when(repository.containsTrack(anyLong())).thenReturn(true);

        boolean result = tagListHandler.canHandle(message);

        assertFalse(result);
    }
}
