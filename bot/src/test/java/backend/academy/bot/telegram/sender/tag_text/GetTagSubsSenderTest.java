package backend.academy.bot.telegram.sender.tag_text;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.sender.tag_text.GetTagSubsSender;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_USER;
import static backend.academy.bot.telegram.handler.Constant.TAG_DELETED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;
import static backend.academy.bot.telegram.handler.Constant.TAG_SUBS_EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetTagSubsSenderTest {

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService scrapperClientService;

    @InjectMocks
    private GetTagSubsSender getTagSubsSender;

    private LinkTrackingObject tracking;

    @BeforeEach
    public void setUp() {
        tracking = new LinkTrackingObject();
    }

    @Test
    public void writeTagAndSendRequest_Success() {
        long chatId = 123L;
        LinkResponse link = new LinkResponse(1L, "url",List.of("tag"), List.of("filter"));
        ListTagLinksResponse res = new ListTagLinksResponse("tag", new ListLinksResponse(List.of(link),1));
        String expected = """
            Ссылки по тегу #tag

            1. url
            Теги:
            #tag
            Фильтры:
            filter""";
        when(scrapperClientService.getSubscriptionsByTag(chatId, "tag")).thenReturn(res);

        String result = getTagSubsSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(expected, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).getSubscriptionsByTag(chatId, "tag");
    }

    @Test
    public void writeTagAndSendRequest_SuccessNoFilters() {
        long chatId = 123L;
        LinkResponse link = new LinkResponse(1L, "url",List.of("tag"), List.of());
        ListTagLinksResponse res = new ListTagLinksResponse("tag", new ListLinksResponse(List.of(link),1));
        String expected = """
            Ссылки по тегу #tag

            1. url
            Теги:
            #tag""";
        when(scrapperClientService.getSubscriptionsByTag(chatId, "tag")).thenReturn(res);

        String result = getTagSubsSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(expected, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).getSubscriptionsByTag(chatId, "tag");
    }

    @Test
    public void writeTagAndSendRequest_SuccessNoLinks() {
        long chatId = 123L;
        ListTagLinksResponse res = new ListTagLinksResponse("tag", new ListLinksResponse(List.of(),0));
        when(scrapperClientService.getSubscriptionsByTag(chatId, "tag")).thenReturn(res);

        String result = getTagSubsSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(TAG_SUBS_EMPTY, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).getSubscriptionsByTag(chatId, "tag");
    }

    @Test
    public void writeTagAndSendRequest_InvalidInput_MoreThanOneTag() {
        long chatId = 123L;
        String result = getTagSubsSender.writeTagAndSendRequest("tag1 tag2", tracking, chatId);

        assertEquals(TAG_INVALID_INPUT, result);
        verify(repository, never()).removeTrack(chatId);
        verify(scrapperClientService, never()).getSubscriptionsByTag(chatId, "tag1 tag2");
    }

    @Test
    public void writeTagAndSendRequest_InvalidInput_TagNotExists() {
        long chatId = 123L;
        ApiErrorResponse response = new ApiErrorResponse("", "400", "ScrapperTagNotExistsException","", List.of());
        when(scrapperClientService.getSubscriptionsByTag(chatId, "tag")).thenThrow(new BotRequestException(response));

        String result = getTagSubsSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(NO_TAG_FOR_USER, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).getSubscriptionsByTag(chatId, "tag");
    }
}
