package backend.academy.bot.telegram;

import backend.academy.bot.telegram.handler.HandlerService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;


@Getter
@Slf4j
public class TelegramBotService extends TelegramBot {

    private final HandlerService handlerService;

    public TelegramBotService(String telegramBotToken, HandlerService handlerService) {
        super(telegramBotToken);
        this.handlerService = handlerService;
    }

    @PostConstruct
    public void startBot() {
        registerCommands();
        setUpdatesListener(list -> {
            for(Update update : list) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void registerCommands() {
        BotCommand[] commands = {
            new BotCommand("/start", "Запуск бота и регистрация пользователя"),
            new BotCommand("/help", "Помощь"),
            new BotCommand("/track", "Добавить ссылку для отслеживания"),
            new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
            new BotCommand("/list", "Список отслеживаемых ссылок")
        };
        SetMyCommands setMyCommands = new SetMyCommands(commands);
        BaseResponse response = execute(setMyCommands);
        if (response.isOk()) {
            log.info("Set My Commands executed successfully");
        } else {
            log.error("Set My Commands failed");
        }
    }

    private void handleUpdate(Update update) {
        Optional<SendMessage> responseMessage = handlerService.handle(update);
        if (responseMessage.isPresent()) {
            SendMessage message = responseMessage.get();
            sendResponse(message);
        }
    }

    public void sendResponse(SendMessage message) {
        SendResponse response = execute(message);
        if (!response.isOk()) {
            throw new RuntimeException("Error sending response: " + response.message());
        }
    }

    @PreDestroy
    public void cleanup() {
        removeGetUpdatesListener();
        this.shutdown();
    }
}
