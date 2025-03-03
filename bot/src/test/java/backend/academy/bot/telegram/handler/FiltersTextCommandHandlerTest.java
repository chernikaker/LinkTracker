package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotInvalidLinkRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.FiltersTextCommandHandler;
import backend.academy.dto.ApiErrorResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FiltersTextCommandHandlerTest {

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private FiltersTextCommandHandler filtersTextCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_AddFiltersAndRegisterSuccessfully() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("filter1 filter2");
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = filtersTextCommandHandler.processRequest(message);

        assertEquals("Фильтры установлены\nСсылка успешно зарегистрирована!", result);
        verify(repository).removeTrack(chatId);
        verify(service).addLinkSubscription(chatId, tracking);
    }

    @Test
    public void processRequest_NoFiltersAndRegisterSuccessfully() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("-");
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = filtersTextCommandHandler.processRequest(message);

        assertEquals("Фильтры не установлены\nСсылка успешно зарегистрирована!", result);
        verify(repository).removeTrack(chatId);
        verify(service).addLinkSubscription(chatId, tracking);
    }

    @Test
    public void processRequest_shouldReturnNotRegisteredMessage_whenUserNotRegistered() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("filter1 filter2");
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));
        doThrow(new BotInvalidLinkRequestException(new ApiErrorResponse(
                        "User not exists", "404", "NotFoundException", "123 not exists", List.of())))
                .when(service)
                .addLinkSubscription(chatId, tracking);

        String result = filtersTextCommandHandler.processRequest(message);

        assertEquals("Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start", result);
        verify(repository).removeTrack(chatId);
        verify(service).addLinkSubscription(chatId, tracking);
    }

    @Test
    public void processRequest_linkIsUnavailable() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("filter1 filter2");
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://unavailable.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));
        doThrow(new BotInvalidLinkRequestException(new ApiErrorResponse(
                        "Unavailable link", "400", "BadRequestException", "unavailable", List.of())))
                .when(service)
                .addLinkSubscription(chatId, tracking);

        String result = filtersTextCommandHandler.processRequest(message);

        assertEquals("Введенная ссылка недоступна, запрос отклонен", result);
        verify(repository).removeTrack(chatId);
        verify(service).addLinkSubscription(chatId, tracking);
    }

    @Test
    public void canHandle_stateIsTrackingFilterAndTextIsNotCommand() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("filter1 filter2");
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = filtersTextCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_StateIsNotTrackingFilter() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("filter1 filter2");

        LinkTrackingObject tracking =
                new LinkTrackingObject("https://example.com", new String[] {"tag1"}, new String[0], UserState.DEFAULT);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = filtersTextCommandHandler.canHandle(message);
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
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = filtersTextCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
