package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotInvalidChatIdException;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.ListCommandHandler;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ListCommandHandlerTest {

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    private ListCommandHandler listCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        listCommandHandler = new ListCommandHandler(repository, service);
    }

    @Test
    public void processRequest_userAndLinksExist() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(repository.containsTrack(chatId)).thenReturn(false);
        ListLinksResponse listLinksResponse = new ListLinksResponse(
                List.of(
                        new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")),
                        new LinkResponse(2L, "https://example.org", List.of("tag2"), List.of("filter2"))),
                2);
        when(service.getUserLinks(chatId)).thenReturn(listLinksResponse);

        String result = listCommandHandler.processRequest(message);

        String expectedMessage =
                """
            Отслеживаемые ссылки:

            https://example.com
            Теги:
            tag1
            Фильтры:
            filter1

            https://example.org
            Теги:
            tag2
            Фильтры:
            filter2

            """;
        assertEquals(expectedMessage, result);
    }

    @Test
    public void processRequest_NoLinksExist() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        when(repository.containsTrack(chatId)).thenReturn(false);
        ListLinksResponse listLinksResponse = new ListLinksResponse(List.of(), 0);
        when(service.getUserLinks(chatId)).thenReturn(listLinksResponse);

        String result = listCommandHandler.processRequest(message);

        assertEquals("Отслеживаемых ссылок нет", result);
        verify(service).getUserLinks(chatId);
    }

    @Test
    public void processRequest_UserNotRegistered() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        when(repository.containsTrack(chatId)).thenReturn(false);
        when(service.getUserLinks(chatId))
                .thenThrow(new BotInvalidChatIdException(new ApiErrorResponse(
                        "User not exists", "404", "NotFoundException", "User not exists", List.of())));

        String result = listCommandHandler.processRequest(message);

        assertEquals("Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start", result);
        verify(service).getUserLinks(chatId);
    }

    @Test
    public void processRequest_RequestFails() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(repository.containsTrack(chatId)).thenReturn(false);
        when(service.getUserLinks(chatId))
                .thenThrow(new BotInvalidChatIdException(new ApiErrorResponse(
                        "Request rejected", "400", "BadRequestException", "Invalid request", List.of())));

        String result = listCommandHandler.processRequest(message);

        assertEquals("Запрос отклонен, попробуйте ещё раз", result);
        verify(service).getUserLinks(chatId);
    }

    @Test
    public void canHandle_shouldReturnTrueForListCommand() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/list");
        when(repository.containsTrack(anyLong())).thenReturn(false);

        boolean result = listCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String request) {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("request");
        when(repository.containsTrack(anyLong())).thenReturn(false);

        boolean result = listCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_whenUserIsTracked() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/list");
        when(repository.containsTrack(anyLong())).thenReturn(true);

        boolean result = listCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
