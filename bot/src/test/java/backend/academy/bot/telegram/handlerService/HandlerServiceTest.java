package backend.academy.bot.telegram.handlerService;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.config.HandlersConfig;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.HandlerService;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import backend.academy.bot.telegram.handler.commands.FiltersTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.HelpCommandHandler;
import backend.academy.bot.telegram.handler.commands.LinkTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.ListCommandHandler;
import backend.academy.bot.telegram.handler.commands.StartCommandHandler;
import backend.academy.bot.telegram.handler.commands.TagsTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.TrackCommandHandler;
import backend.academy.bot.telegram.handler.commands.UnknownCommandHandler;
import backend.academy.bot.telegram.handler.commands.UntrackCommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ContextConfiguration(classes = HandlerService.class)
@Import(HandlersConfig.class)
public class HandlerServiceTest {

    @MockitoBean
    private InMemoryTrackingCache cache;

    @MockitoBean
    private ScrapperClientService service;

    @Autowired
    private HandlerService handlerService;

    @Test
    public void getHandler_StartCommand() {
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/start");

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(StartCommandHandler.class, handler);
    }

    @Test
    public void getHandler_HelpCommand() {
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/help");

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(HelpCommandHandler.class, handler);
    }

    @Test
    public void getHandler_ListCommand() {
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/list");

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(ListCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TrackCommand_CorrectTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/track");
        when(cache.containsTrack(chatId)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(TrackCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TrackCommand_WrongTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/track");
        when(cache.containsTrack(chatId)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_UntrackCommand_CorrectTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/untrack");
        when(cache.containsTrack(chatId)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UntrackCommandHandler.class, handler);
    }

    @Test
    public void getHandler_UntrackCommand_IncorrectTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/untrack");
        when(cache.containsTrack(chatId)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TrackingLinkText_CorrectTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("some_text");
        when(cache.containsTrack(chatId)).thenReturn(true);
        LinkTrackingObject trackingObject =
                new LinkTrackingObject("", new String[0], new String[0], UserState.TRACKING_LINK);
        when(cache.getTrack(chatId)).thenReturn(Optional.of(trackingObject));

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(LinkTextCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TrackingTags_CorrectTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("some tags");
        when(cache.containsTrack(chatId)).thenReturn(true);
        LinkTrackingObject trackingObject =
                new LinkTrackingObject("", new String[0], new String[0], UserState.TRACKING_TAG);
        when(cache.getTrack(chatId)).thenReturn(Optional.of(trackingObject));

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(TagsTextCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TrackingFilters_CorrectTrackingState() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("some filters");
        when(cache.containsTrack(chatId)).thenReturn(true);
        LinkTrackingObject trackingObject =
                new LinkTrackingObject("", new String[0], new String[0], UserState.TRACKING_FILTER);
        when(cache.getTrack(chatId)).thenReturn(Optional.of(trackingObject));

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(FiltersTextCommandHandler.class, handler);
    }

    @Test
    public void getHandler_RandomTextInput() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("text");
        when(cache.containsTrack(chatId)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }
}
