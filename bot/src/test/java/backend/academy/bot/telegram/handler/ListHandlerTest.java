package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.ListHandler;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
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

public class ListHandlerTest {

    private static final long CHAT_ID = 123L;

    private static final String EXPECTED =
            """
Отслеживаемые ссылки:

https://example.com
Теги:
#tag1
Фильтры:
filter1

https://example.org
Теги:
#tag2
Фильтры:
filter2

""";

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private ListHandler listHandler;

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
    public void processRequest_userAndLinksExist() {
        ListLinksResponse listLinksResponse = new ListLinksResponse(
                List.of(
                        new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")),
                        new LinkResponse(2L, "https://example.org", List.of("tag2"), List.of("filter2"))),
                2);
        when(service.getUserLinks(CHAT_ID)).thenReturn(listLinksResponse);

        String result = listHandler.processRequest(message);

        assertEquals(EXPECTED, result);
    }

    @Test
    public void processRequest_NoLinksExist() {
        ListLinksResponse listLinksResponse = new ListLinksResponse(List.of(), 0);
        when(service.getUserLinks(CHAT_ID)).thenReturn(listLinksResponse);

        String result = listHandler.processRequest(message);

        assertEquals(Constant.NO_LINKS, result);
        verify(service).getUserLinks(CHAT_ID);
    }

    @Test
    public void processRequest_UserNotRegistered() {
        when(service.getUserLinks(CHAT_ID))
                .thenThrow(new BotRequestException(new ApiErrorResponse(
                        "User not exists", "404", "NotFoundException", "User not exists", List.of())));

        String result = listHandler.processRequest(message);

        assertEquals(Constant.NOT_REGISTERED, result);
        verify(service).getUserLinks(CHAT_ID);
    }

    @Test
    public void processRequest_RequestFails() {
        when(service.getUserLinks(CHAT_ID))
                .thenThrow(new BotRequestException(new ApiErrorResponse(
                        "Request rejected", "400", "BadRequestException", "Invalid request", List.of())));

        String result = listHandler.processRequest(message);

        assertEquals(Constant.REQUEST_CANCELLED, result);
        verify(service).getUserLinks(CHAT_ID);
    }

    @Test
    public void canHandle_shouldReturnTrueForListCommand() {
        when(message.text()).thenReturn("/list");
        when(repository.containsTrack(anyLong())).thenReturn(false);

        boolean result = listHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String request) {
        when(message.text()).thenReturn(request);
        when(repository.containsTrack(anyLong())).thenReturn(false);

        boolean result = listHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_whenUserIsTracked() {
        when(message.text()).thenReturn("/list");
        when(repository.containsTrack(anyLong())).thenReturn(true);

        boolean result = listHandler.canHandle(message);

        assertFalse(result);
    }
}
