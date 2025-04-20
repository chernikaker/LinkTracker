package backend.academy.bot.telegram.sender.tag_text;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.model.UserState;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.Command;
import backend.academy.bot.telegram.handler.Constant;
import backend.academy.bot.telegram.handler.commands.TagsTextHandler;
import backend.academy.bot.telegram.handler.sender.tag_text.AddTagsToSubSender;
import backend.academy.bot.telegram.handler.sender.tag_text.DeleteTagSender;
import backend.academy.bot.telegram.handler.sender.tag_text.GetTagSubsSender;
import backend.academy.bot.telegram.handler.sender.tag_text.RemoveTagFromSubSender;
import backend.academy.bot.telegram.handler.sender.tag_text.TagCommandSenderFactory;
import backend.academy.bot.telegram.handler.sender.tag_text.UntrackByTagSender;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import java.util.Optional;
import static backend.academy.bot.telegram.handler.Constant.EMPTY_INPUT;
import static backend.academy.bot.telegram.handler.Constant.NO_TAGS;
import static backend.academy.bot.telegram.handler.Constant.SUBS_DELETED_BY_TAG;
import static backend.academy.bot.telegram.handler.Constant.TAGS_ADDED_AND_SENT;
import static backend.academy.bot.telegram.handler.Constant.TAG_DELETED;
import static backend.academy.bot.telegram.handler.Constant.TAG_DETACHED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;
import static backend.academy.bot.telegram.handler.Constant.TAG_SUBS_EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {
    AddTagsToSubSender.class,
    DeleteTagSender.class,
    RemoveTagFromSubSender.class,
    TagCommandSenderFactory.class,
    UntrackByTagSender.class,
    GetTagSubsSender.class,
    TagsTextHandler.class
})
public class TagTextHandlerIntegrationTest {

    private static final long CHAT_ID = 123L;
    public static final LinkTrackingObject TRACKING = new LinkTrackingObject("https://example.com", new String[0], new String[0], UserState.TRACKING_TAG, Command.TRACK);

    @MockitoBean
    private InMemoryTrackingCache repository;

    @MockitoBean
    private ScrapperClientService service;

    @Autowired
    private TagsTextHandler tagsTextHandler;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @BeforeEach
    public void setUp() {
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
    }

    @Test
    public void processRequest_RemoveTagSub_Success() {
        when(message.text()).thenReturn("tag");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.REMOVE_TAG_SUB);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_DETACHED, result);
    }

    @Test
    public void processRequest_RemoveTagSub_InvalidInput() {
        when(message.text()).thenReturn("tag1 tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.REMOVE_TAG_SUB);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_INVALID_INPUT, result);
    }

    @Test
    public void processRequest_AddTagsToSub_Success() {
        when(message.text()).thenReturn("tag1 tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.TAGS_TO_SUB);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAGS_ADDED_AND_SENT, result);
    }

    @Test
    public void processRequest_DeleteTag_Success() {
        when(message.text()).thenReturn("tag");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.REMOVE_TAG);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_DELETED, result);
    }

    @Test
    public void processRequest_DeleteTag_InvalidInput() {
        when(message.text()).thenReturn("tag1, tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.REMOVE_TAG);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_INVALID_INPUT, result);
    }

    @Test
    public void processRequest_GetTagSubs_Success() {
        when(message.text()).thenReturn("tag");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.LIST_BY_TAG);
        LinkResponse link = new LinkResponse(1L, "url", List.of("tag"), List.of());
        ListTagLinksResponse res = new ListTagLinksResponse("tag", new ListLinksResponse(List.of(link),1));
        String expected = """
            Ссылки по тегу #tag

            1. url
            Теги:
            #tag""";
        when(service.getSubscriptionsByTag(CHAT_ID, "tag")).thenReturn(res);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(expected, result);
    }

    @Test
    public void processRequest_GetTagSubs_SuccessNoSubs() {
        when(message.text()).thenReturn("tag");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.LIST_BY_TAG);
        ListTagLinksResponse res = new ListTagLinksResponse("tag", new ListLinksResponse(List.of(),0));
        when(service.getSubscriptionsByTag(CHAT_ID, "tag")).thenReturn(res);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_SUBS_EMPTY, result);
    }

    @Test
    public void processRequest_GetTagSubs_InvalidInput() {
        when(message.text()).thenReturn("tag1, tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.LIST_BY_TAG);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_INVALID_INPUT, result);
    }

    @Test
    public void processRequest_UntrackByTag_Success() {
        when(message.text()).thenReturn("tag");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.UNTRACK_BY_TAG);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(SUBS_DELETED_BY_TAG, result);
    }

    @Test
    public void processRequest_UntrackByTag_InvalidInput() {
        when(message.text()).thenReturn("tag1, tag2");
        when(repository.getTrack(CHAT_ID)).thenReturn(Optional.of(TRACKING));
        TRACKING.command(Command.UNTRACK_BY_TAG);

        String result = tagsTextHandler.processRequest(message);

        assertEquals(TAG_INVALID_INPUT, result);
    }
}
