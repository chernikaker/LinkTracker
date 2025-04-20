package backend.academy.bot.telegram.sender.tag_text;

import static backend.academy.bot.telegram.handler.Constant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.model.LinkTrackingObject;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.sender.tag_text.AddTagsToSubSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AddTagToSubSenderTest {

    @Mock
    private InMemoryTrackingCache repository;

    @Mock
    private ScrapperClientService scrapperClientService;

    @InjectMocks
    private AddTagsToSubSender addTagsToSubSender;

    private LinkTrackingObject tracking;

    @BeforeEach
    public void setUp() {
        tracking = new LinkTrackingObject();
    }

    @Test
    public void writeTagAndSendRequest_Success() {
        long chatId = 123L;
        String result = addTagsToSubSender.writeTagAndSendRequest("tag", tracking, chatId);

        assertEquals(TAGS_ADDED_AND_SENT, result);
        assertThat(tracking.tags()).containsExactly("tag");
        verify(repository).removeTrack(chatId);
        verify(scrapperClientService).addTagsToSubscription(chatId, tracking);
    }
}
