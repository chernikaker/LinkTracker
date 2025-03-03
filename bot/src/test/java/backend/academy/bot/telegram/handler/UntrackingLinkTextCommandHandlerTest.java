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

    public static final String gitRepo = "https://github.com/owner/repo";
    public static final String EXAMPLE_LINK = "https://example.com";

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService service;

    @InjectMocks
    private UntrackingLinkTextCommandHandler untrackingLinkTextCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_removeLinkAndReturnSuccessMessage() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(gitRepo);
        LinkTrackingObject tracking =
                new LinkTrackingObject(gitRepo, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals("Ссылка успешно удалена", result);
        verify(repository).removeTrack(chatId);
        verify(service).removeLinkSubscription(chatId, gitRepo);
    }

    @Test
    public void processRequest_shouldReturnErrorMessage_whenLinkIsInvalid() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("invalid-link");
        LinkTrackingObject tracking =
                new LinkTrackingObject("invalid-link", new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals(Constant.LINK_NOT_VALID, result);
        verify(repository, never()).removeTrack(chatId);
        verify(service, never()).removeLinkSubscription(anyLong(), anyString());
    }

    @Test
    public void processRequest_userNotRegistered() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(gitRepo);
        LinkTrackingObject tracking =
                new LinkTrackingObject(gitRepo, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));
        doThrow(new BotInvalidLinkRequestException(new ApiErrorResponse(
                        "User not exists", "404", "NotFoundException", "123 not exists", List.of())))
                .when(service)
                .removeLinkSubscription(chatId, gitRepo);

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals("Вы не зарегистрированы. Чтобы зарегистрироваться, выполните /start", result);
        verify(repository).removeTrack(chatId);
        verify(service).removeLinkSubscription(chatId, gitRepo);
    }

    @Test
    public void processRequest_shouldReturnNoSubscriptionMessage_whenLinkNotFound() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(gitRepo);
        LinkTrackingObject tracking =
                new LinkTrackingObject(gitRepo, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));
        doThrow(new BotInvalidLinkRequestException(new ApiErrorResponse(
                        "Link not found", "404", "NotFoundException", "Link not found", List.of())))
                .when(service)
                .removeLinkSubscription(chatId, gitRepo);

        String result = untrackingLinkTextCommandHandler.processRequest(message);

        assertEquals("У вас нет подписки на данную ссылку", result);
        verify(repository).removeTrack(chatId);
        verify(service).removeLinkSubscription(chatId, gitRepo);
    }

    @Test
    public void canHandle_shouldReturnTrue_stateIsUntrackingLinkTextIsNotCommand() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(EXAMPLE_LINK);
        LinkTrackingObject tracking =
                new LinkTrackingObject(EXAMPLE_LINK, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = untrackingLinkTextCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @Test
    public void canHandle_shouldReturnFalse_stateIsNotUntrackingLink() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(EXAMPLE_LINK);
        LinkTrackingObject tracking =
                new LinkTrackingObject(EXAMPLE_LINK, new String[0], new String[0], UserState.DEFAULT);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));

        boolean result = untrackingLinkTextCommandHandler.canHandle(message);

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
                new LinkTrackingObject(EXAMPLE_LINK, new String[0], new String[0], UserState.UNTRACKING_LINK);
        when(repository.getTrack(chatId)).thenReturn(Optional.of(tracking));
        boolean result = untrackingLinkTextCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
