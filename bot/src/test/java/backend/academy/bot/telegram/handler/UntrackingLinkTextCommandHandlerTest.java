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
import backend.academy.bot.telegram.handler.commands.UntrackingLinkTextCommandHandler;
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

public class UntrackingLinkTextCommandHandlerTest {

    private static final String gitRepo = "https://github.com/owner/repo";
    private static final String EXAMPLE_LINK = "https://example.com";
    private static final long CHAT_ID = 123L;

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private UntrackingLinkTextCommandHandler untrackingLinkTextCommandHandler;

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
    public void processRequest_removeLinkAndReturnSuccessMessage() {
        when(message.text()).thenReturn(gitRepo);
        LinkTrackingObject tracking =
                new LinkTrackingObject(gitRepo, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals("Ссылка успешно удалена", result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).removeLinkSubscription(CHAT_ID, gitRepo);
    }

    @Test
    public void processRequest_shouldReturnErrorMessage_whenLinkIsInvalid() {
        when(message.text()).thenReturn("invalid-link");
        LinkTrackingObject tracking =
                new LinkTrackingObject("invalid-link", new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals(Constant.LINK_NOT_VALID, result);
        verify(repository, never()).removeTrack(CHAT_ID);
        verify(service, never()).removeLinkSubscription(anyLong(), anyString());
    }

    @Test
    public void processRequest_userNotRegistered() {
        when(message.text()).thenReturn(gitRepo);
        LinkTrackingObject tracking =
                new LinkTrackingObject(gitRepo, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "User not exists", "404", "NotFoundException", "123 not exists", List.of())))
                .when(service)
                .removeLinkSubscription(CHAT_ID, gitRepo);

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals(Constant.NOT_REGISTERED, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).removeLinkSubscription(CHAT_ID, gitRepo);
    }

    @Test
    public void processRequest_shouldReturnNoSubscriptionMessage_whenLinkNotFound() {
        when(message.text()).thenReturn(gitRepo);
        LinkTrackingObject tracking =
                new LinkTrackingObject(gitRepo, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));
        doThrow(new BotRequestException(new ApiErrorResponse(
                        "Link not found", "404", "NotFoundException", "Link not found", List.of())))
                .when(service)
                .removeLinkSubscription(CHAT_ID, gitRepo);

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals(Constant.NO_SUBSCRIPTION, result);
        verify(repository).removeTrack(CHAT_ID);
        verify(service).removeLinkSubscription(CHAT_ID, gitRepo);
    }

    @Test
    public void canHandle_shouldReturnTrue_stateIsUntrackingLinkTextIsNotCommand() {
        when(message.text()).thenReturn(EXAMPLE_LINK);
        LinkTrackingObject tracking =
                new LinkTrackingObject(EXAMPLE_LINK, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));

        boolean result = untrackingLinkTextCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_stateIsNotUntrackingLink() {
        when(message.text()).thenReturn(EXAMPLE_LINK);
        LinkTrackingObject tracking =
                new LinkTrackingObject(EXAMPLE_LINK, new String[0], new String[0], UserState.DEFAULT);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));

        boolean result = untrackingLinkTextCommandHandler.canHandle(message);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({"/help", "/track", "/start", "/any"})
    public void canHandle_shouldReturnFalse_whenTextIsCommand(String command) {
        when(message.text()).thenReturn(command);
        LinkTrackingObject tracking =
                new LinkTrackingObject(EXAMPLE_LINK, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(tracking));
        boolean result = untrackingLinkTextCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
