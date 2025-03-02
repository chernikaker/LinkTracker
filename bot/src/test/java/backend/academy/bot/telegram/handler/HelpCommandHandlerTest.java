package backend.academy.bot.telegram.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.telegram.handler.commands.HelpCommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HelpCommandHandlerTest {

    @Mock
    private InMemoryTrackingCache repository;

    @InjectMocks
    private HelpCommandHandler helpCommandHandler;

    private String expectedMessage =
            """
            Бот поддерживает следующие ссылки и обновления:

            GITHUB
            Ссылки вида:
            https://github.com/{владелец-репозитория}/{название-репозитория}
            * - также можно использовать http://
            Обновления:
            - commit
            - issue
            - comment

            STACKOVERFLOW
            Ссылки вида:
            https://stackoverflow.com/questions/{id-вопроса}/{название-вопроса}
            https://stackoverflow.com/questions/{id-вопроса}
            * - также можно использовать http://
            Обновления:
            - answer
            - comment
            """;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processRequest_ReturnHelpMessage() {
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/help");
        when(repository.containsTrack(chatId)).thenReturn(false);

        String result = helpCommandHandler.processRequest(message);

        assertEquals(expectedMessage, result);
        verify(repository, never()).removeTrack(chatId);
    }

    @Test
    public void processRequest_shouldRemoveTrackAndReturnHelpMessage() {
        // Arrange
        long chatId = 123L;
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/help");
        when(repository.containsTrack(chatId)).thenReturn(true);

        String result = helpCommandHandler.processRequest(message);

        assertEquals(expectedMessage, result);
        verify(repository).removeTrack(chatId);
    }

    @Test
    public void canHandle_shouldReturnTrueForHelpCommand() {
        Message message = mock(Message.class);
        when(message.text()).thenReturn("/help");

        boolean result = helpCommandHandler.canHandle(message);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({"/start", "/track", "text"})
    public void canHandle_shouldReturnFalseForOtherCommands(String command) {
        Message message = mock(Message.class);
        when(message.text()).thenReturn("command");

        boolean result = helpCommandHandler.canHandle(message);

        assertFalse(result);
    }
}
