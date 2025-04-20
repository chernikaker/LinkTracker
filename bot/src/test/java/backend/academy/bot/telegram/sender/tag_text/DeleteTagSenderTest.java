package backend.academy.bot.telegram.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.NO_TAG_FOR_USER;
import static backend.academy.bot.telegram.handler.Constant.TAG_DELETED;
import static backend.academy.bot.telegram.handler.Constant.TAG_INVALID_INPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.exception.scrapperClient.BotRequestException;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.sender.tag_text.DeleteTagSender;
import backend.academy.dto.ApiErrorResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteTagSenderTest {

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService scrapperClientService;

    @InjectMocks
    private DeleteTagSender deleteTagSender;

    private LinkTrackingObject tracking;

    @BeforeEach
    public void setUp() {
        tracking = new LinkTrackingObject();
    }

    @Test
    public void writeTagAndSendRequest_Success() {
        long chatId = 123L;
        String result = deleteTagSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(TAG_DELETED, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).deleteTag(chatId, "tag");
    }

    @Test
    public void writeTagAndSendRequest_InvalidInput_MoreThanOneTag() {
        long chatId = 123L;
        String result = deleteTagSender.writeTagAndSendRequest("tag1 tag2", tracking, chatId);

        assertEquals(TAG_INVALID_INPUT, result);
        verify(repository, never()).removeTrack(chatId);
        verify(scrapperClientService, never()).deleteTag(chatId, "tag1 tag2");
    }

    @Test
    public void writeTagAndSendRequest_InvalidInput_TagNotExists() {
        long chatId = 123L;
        ApiErrorResponse response = new ApiErrorResponse("", "404", "", "", List.of());
        doThrow(new BotRequestException(response)).when(scrapperClientService).deleteTag(chatId, "tag");

        String result = deleteTagSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(NO_TAG_FOR_USER, result);
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).deleteTag(chatId, "tag");
    }
}
