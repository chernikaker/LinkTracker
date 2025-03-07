package backend.academy.bot.telegram;

import backend.academy.bot.telegram.handler.Command;
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
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Основной класс телеграм бота, получает и отправляет сообщения */
@Getter
@Slf4j
public class TelegramBotService extends TelegramBot {

    /** Сервис для выбора обработчика сообщения */
    private final HandlerService handlerService;

    public TelegramBotService(String telegramBotToken, HandlerService handlerService) {
        super(telegramBotToken);
        this.handlerService = handlerService;
    }

    @PostConstruct
    public void startBot() {
        registerCommands();
        setUpdatesListener(list -> {
            for (Update update : list) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /** Регистрация доступных команд */
    private void registerCommands() {
        BotCommand[] commands = {
            new BotCommand(Command.START.command(), Command.START.description()),
            new BotCommand(Command.HELP.command(), Command.HELP.description()),
            new BotCommand(Command.TRACK.command(), Command.TRACK.description()),
            new BotCommand(Command.UNTRACK.command(), Command.UNTRACK.description()),
            new BotCommand(Command.LIST.command(), Command.LIST.description())
        };
        SetMyCommands setMyCommands = new SetMyCommands(commands);
        BaseResponse response = execute(setMyCommands);
        if (response.isOk()) {
            log.atInfo().log("Set My Commands executed successfully");
        } else {
            log.atError().log("Set My Commands failed");
        }
    }

    /**
     * Обработка обновлений с помощью HandlerService. Отправляет сообщение в случае ответа на обновление.
     *
     * @param update входное обновление
     */
    private void handleUpdate(Update update) {
        Optional<SendMessage> responseMessage = handlerService.handle(update);
        if (responseMessage.isPresent()) {
            SendMessage message = responseMessage.orElseThrow();
            sendResponse(message);
        }
    }

    /**
     * Метод отправки сообщения пользователю
     *
     * @param message сообщение
     */
    public void sendResponse(SendMessage message) {
        SendResponse response = execute(message);
        if (!response.isOk()) {
            log.atError().addKeyValue("message", message).log("Error sending response message");
        }
    }

    @PreDestroy
    public void cleanup() {
        removeGetUpdatesListener();
        this.shutdown();
    }
}
