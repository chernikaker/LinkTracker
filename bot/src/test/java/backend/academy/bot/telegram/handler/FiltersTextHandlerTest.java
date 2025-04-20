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
import backend.academy.bot.telegram.handler.commands.FiltersTextHandler;
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

public class FiltersTextHandlerTest {

    public static final LinkTrackingObject TRACKING = new LinkTrackingObject(
            "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER, Command.TRACK);
    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private FiltersTextHandler filtersTextHandler;

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
    public void processRequest_AddFiltersAndRegisterSuccessfully() {
        when(message.text()).thenReturn("filter1 filter2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        String result = filtersTextHandler.processRequest(message);

        assertEquals(Constant.FILTERS_REGISTERED + Constant.LINK_REGISTERED, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).addLinkSubscription(CHAT_ID, TRACKING);
    }

    @Test
    public void processRequest_NoFiltersAndRegisterSuccessfully() {
        when(message.text()).thenReturn("-");
        LinkTrackingObject tracking = new LinkTrackingObject(
                "https://example.com", new String[] {"tag1"}, new String[0], UserState.TRACKING_FILTER, Command.TRACK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));

        String result = filtersTextHandler.processRequest(message);

        assertEquals(Constant.FILTERS_NOT_REGISTERD + Constant.LINK_REGISTERED, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).addLinkSubscription(CHAT_ID, tracking);
    }

    @Test
    public void processRequest_shouldReturnNotRegisteredMessage_whenUserNotRegistered() {
        when(message.text()).thenReturn("filter1 filter2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "", "400", "ScrapperUserNotExistsException", "", List.of())))
                .when(service)
                .addLinkSubscription(CHAT_ID, TRACKING);

        String result = filtersTextHandler.processRequest(message);

        assertEquals(Constant.NOT_REGISTERED, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).addLinkSubscription(CHAT_ID, TRACKING);
    }

    @Test
    public void processRequest_linkIsUnavailable() {
        when(message.text()).thenReturn("filter1 filter2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "Unavailable link", "400", "ScrapperUnavailableLinkException", "", List.of())))
                .when(service)
                .addLinkSubscription(CHAT_ID, TRACKING);

        String result = filtersTextHandler.processRequest(message);

        assertEquals(Constant.LINK_UNABAILABLE, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).addLinkSubscription(CHAT_ID, TRACKING);
    }

    @Test
    public void canHandle_stateIsTrackingFilterAndTextIsNotCommand() {
        when(message.text()).thenReturn("filter1 filter2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.state(UserState.TRACKING_FILTER);

        boolean result = filtersTextHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_StateIsNotTrackingFilter() {
        when(message.text()).thenReturn("filter1 filter2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.state(UserState.DEFAULT);

        boolean result = filtersTextHandler.canHandle(message);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "/any"})
    public void canHandle_shouldReturnFalse_whenTextIsCommand(String command) {
        when(message.text()).thenReturn(command);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));

        boolean result = filtersTextHandler.canHandle(message);

        assertFalse(result);
    }
}
