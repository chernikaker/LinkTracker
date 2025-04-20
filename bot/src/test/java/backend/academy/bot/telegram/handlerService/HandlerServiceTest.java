package backend.academy.bot.telegram.handlerService;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.config.HandlersConfig;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.bot.telegram.handler.HandlerService;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import backend.academy.bot.telegram.handler.commands.FiltersTextHandler;
import backend.academy.bot.telegram.handler.commands.HelpHandler;
import backend.academy.bot.telegram.handler.commands.LinkTextHandler;
import backend.academy.bot.telegram.handler.commands.ListByTagHandler;
import backend.academy.bot.telegram.handler.commands.ListHandler;
import backend.academy.bot.telegram.handler.commands.RemoveTagFromSubHandler;
import backend.academy.bot.telegram.handler.commands.RemoveTagHandler;
import backend.academy.bot.telegram.handler.commands.StartHandler;
import backend.academy.bot.telegram.handler.commands.TagListHandler;
import backend.academy.bot.telegram.handler.commands.TagsTextHandler;
import backend.academy.bot.telegram.handler.commands.TagsToSubHandler;
import backend.academy.bot.telegram.handler.commands.TrackHandler;
import backend.academy.bot.telegram.handler.commands.UnknownCommandHandler;
import backend.academy.bot.telegram.handler.commands.UntrackByTagHandler;
import backend.academy.bot.telegram.handler.commands.UntrackHandler;
import backend.academy.bot.telegram.handler.sender.tag_text.TagCommandSenderFactory;
import backend.academy.bot.telegram.handler.sender.tag_text.TagTextSender;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ContextConfiguration(classes = HandlerService.class)
@Import(HandlersConfig.class)
public class HandlerServiceTest {

    public static final LinkTrackingObject TRACKING_OBJECT = new LinkTrackingObject("", new String[0], new String[0], UserState.TRACKING_LINK, Command.TRACK);
    private static final long CHAT_ID = 123L;

    @MockitoBean
    private InMemoryTrackingCache cache;

    @MockitoBean
    private ScrapperClientService service;

    @MockitoBean
    private TagCommandSenderFactory factory;

    @Autowired
    private HandlerService handlerService;

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
    public void getHandler_StartCommand() {
        when(message.text()).thenReturn("/start");

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(StartHandler.class, handler);
    }

    @Test
    public void getHandler_HelpCommand() {
        when(message.text()).thenReturn("/help");

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(HelpHandler.class, handler);
    }

    @Test
    public void getHandler_ListCommand() {
        when(message.text()).thenReturn("/list");

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(ListHandler.class, handler);
    }

    @Test
    public void getHandler_TrackCommand_CorrectTrackingState() {
        when(message.text()).thenReturn("/track");
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(TrackHandler.class, handler);
    }

    @Test
    public void getHandler_TrackCommand_WrongTrackingState() {
        when(message.text()).thenReturn("/track");
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_ListByTagCommand_CorrectTrackingState() {
        when(message.text()).thenReturn(Command.LIST_BY_TAG.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(ListByTagHandler.class, handler);
    }

    @Test
    public void getHandler_ListByTagCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn(Command.LIST_BY_TAG.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_RemoveTagFromSubCommand_CorrectTrackingState() {
        when(message.text()).thenReturn(Command.REMOVE_TAG_SUB.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(RemoveTagFromSubHandler.class, handler);
    }

    @Test
    public void getHandler_RemoveTagFromSubCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn(Command.REMOVE_TAG_SUB.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_RemoveTagCommand_CorrectTrackingState() {
        when(message.text()).thenReturn(Command.REMOVE_TAG.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(RemoveTagHandler.class, handler);
    }

    @Test
    public void getHandler_RemoveTagCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn(Command.REMOVE_TAG.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TagListCommand_CorrectTrackingState() {
        when(message.text()).thenReturn(Command.TAGS.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(TagListHandler.class, handler);
    }

    @Test
    public void getHandler_TagListCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn(Command.TAGS.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_AddTagsToSubCommand_CorrectTrackingState() {
        when(message.text()).thenReturn(Command.TAGS_TO_SUB.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(TagsToSubHandler.class, handler);
    }

    @Test
    public void getHandler_AddTagsToSubCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn(Command.TAGS_TO_SUB.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_UntrackByTagCommand_CorrectTrackingState() {
        when(message.text()).thenReturn(Command.UNTRACK_BY_TAG.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UntrackByTagHandler.class, handler);
    }

    @Test
    public void getHandler_UntrackByTagCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn(Command.UNTRACK_BY_TAG.command());
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_UntrackCommand_CorrectTrackingState() {
        when(message.text()).thenReturn("/untrack");
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UntrackHandler.class, handler);
    }

    @Test
    public void getHandler_UntrackCommand_IncorrectTrackingState() {
        when(message.text()).thenReturn("/untrack");
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }

    @Test
    public void getHandler_TrackingLinkText_CorrectTrackingState() {
        when(message.text()).thenReturn("some_text");
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);
        TRACKING_OBJECT.state(UserState.TRACKING_LINK);
        when(cache.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING_OBJECT));

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(LinkTextHandler.class, handler);
    }

    @Test
    public void getHandler_TrackingTags_CorrectTrackingState() {
        when(message.text()).thenReturn("some tags");
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);
        TRACKING_OBJECT.state(UserState.TRACKING_TAG);
        when(cache.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING_OBJECT));

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(TagsTextHandler.class, handler);
    }

    @Test
    public void getHandler_TrackingFilters_CorrectTrackingState() {
        when(message.text()).thenReturn("some filters");
        when(cache.containsTrack(CHAT_ID)).thenReturn(true);
        TRACKING_OBJECT.state(UserState.TRACKING_FILTER);
        when(cache.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING_OBJECT));

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(FiltersTextHandler.class, handler);
    }

    @Test
    public void getHandler_RandomTextInput() {
        when(message.text()).thenReturn("text");
        when(cache.containsTrack(CHAT_ID)).thenReturn(false);

        CommandHandler handler = handlerService.getHandlerByMessage(message);

        assertInstanceOf(UnknownCommandHandler.class, handler);
    }
}
