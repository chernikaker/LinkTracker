package backend.academy.bot.telegram.sender.tag_text;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.sender.tag_text.UntrackByTagSender;
import backend.academy.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_SUBSCRIPTION;
import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_USER;
import static backend.academy.bot.telegram.handler.Constant.SUBS_DELETED_BY_TAG;
import static backend.academy.bot.telegram.handler.Constant.TAG_DETACHED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UntrackByTagSenderTest {
    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService scrapperClientService;

    @InjectMocks
    private UntrackByTagSender untrackByTagSender;

    private LinkTrackingObject tracking;

    @BeforeEach
    public void setUp() {
        tracking = new LinkTrackingObject();
    }

    @Test
    public void writeTagAndSendRequest_Success() {
        long chatId = 123L;
        String result = untrackByTagSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(SUBS_DELETED_BY_TAG, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).removeSubscriptionsByTag(chatId, "tag");
    }

    @Test
    public void writeTagAndSendRequest_InvalidInput_MoreThanOneTag() {
        long chatId = 123L;
        String result = untrackByTagSender.writeTagAndSendRequest("tag1 tag2", tracking, chatId);

        assertEquals(TAG_INVALID_INPUT, result);
        verify(repository, never()).removeTrack(chatId);
        verify(scrapperClientService, never()).removeSubscriptionsByTag(chatId, "tag1 tag2");
    }

    @Test
    public void writeTagAndSendRequest_InvalidInput_TagNotExists() {
        long chatId = 123L;
        ApiErrorResponse response = new ApiErrorResponse("", "400", "ScrapperTagNotExistException","", List.of());
        doThrow(new BotRequestException(response)).when(scrapperClientService).removeSubscriptionsByTag(chatId, "tag");

        String result = untrackByTagSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(NO_TAG_FOR_USER, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).removeSubscriptionsByTag(chatId, "tag");
    }
}
