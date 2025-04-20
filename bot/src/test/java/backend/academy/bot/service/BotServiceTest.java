package backend.academy.bot.service;

import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static backend.academy.data.Constant.MAX_DESCRIPTION_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    private static final long CHAT_ID = 12345L;
    private static final String TEST_URL = "https://example.com";
    private static final String TEST_TAG = "test_tag";
    private static final LinkUpdateUnit UPDATE_UNIT = new LinkUpdateUnit(
        "title",
        "description",
        LocalDateTime.now(ZoneId.systemDefault()),
        "author",
        "comment"
    );

    private static final LinkUpdate UPDATE = new LinkUpdate(
        1L,
        TEST_URL,
        List.of(UPDATE_UNIT),
        Map.of(CHAT_ID, List.of(TEST_TAG))
    );

    @Mock
    private TelegramBotService telegramBotService;

    @InjectMocks
    private BotService botService;

    @Captor
    private ArgumentCaptor<SendMessage> messageCaptor;

    @Test
    public void sendUpdates_SendsCorrectMessageWithPreview() {
        botService.sendUpdates(UPDATE);

        verify(telegramBotService).sendResponse(messageCaptor.capture());
        SendMessage actualMessage = messageCaptor.getValue();

        assertEquals(CHAT_ID, actualMessage.getParameters().get("chat_id"));
        assertTrue(actualMessage.getParameters().get("text").toString().contains(TEST_URL));

        LinkPreviewOptions previewOptions = (LinkPreviewOptions) actualMessage.getParameters().get("link_preview_options");
        assertNotNull(previewOptions);
        assertEquals(TEST_URL, previewOptions.url());
        assertFalse(previewOptions.preferSmallMedia());
        assertFalse(previewOptions.showAboveText());
    }

    @Test
    public void createUpdatesMessage_FormatsCorrectly() {
        String result = botService.createUpdatesMessage(
            TEST_URL,
            List.of(UPDATE_UNIT),
            List.of(TEST_TAG)
        );
        assertTrue(result.contains(TEST_URL));
        assertTrue(result.contains("#1"));
        assertTrue(result.contains("Тип обновления: comment"));
        assertTrue(result.contains("Заголовок: title"));
        assertTrue(result.contains("Автор: author"));
        assertTrue(result.contains("Сообщение: description"));
        assertTrue(result.contains("#" + TEST_TAG));
    }

    @Test
    public void createUpdatesMessage_ShowsIfLongDescription() {
        String longDescription = "a".repeat(MAX_DESCRIPTION_LENGTH);

        LinkUpdateUnit updateUnit = new LinkUpdateUnit(
            "title",
            longDescription,
            LocalDateTime.now(ZoneId.systemDefault()),
            "author",
            "comment"
        );
        String result = botService.createUpdatesMessage(TEST_URL, List.of(updateUnit), List.of(TEST_TAG));

        assertTrue(result.contains("Сообщение: " + "a".repeat(MAX_DESCRIPTION_LENGTH) + "..."));
    }

    @Test
    void sendUpdateInfo_SendsMessageWithCorrectPreview() {
        String testMessage = "Test message";

        botService.sendUpdateInfo(CHAT_ID, testMessage, TEST_URL);

        verify(telegramBotService).sendResponse(messageCaptor.capture());
        SendMessage actualMessage = messageCaptor.getValue();

        assertEquals(CHAT_ID, actualMessage.getParameters().get("chat_id"));
        assertEquals(testMessage, actualMessage.getParameters().get("text"));

        LinkPreviewOptions previewOptions = (LinkPreviewOptions) actualMessage.getParameters().get("link_preview_options");
        assertNotNull(previewOptions);
        assertEquals(TEST_URL, previewOptions.url());
    }
}
