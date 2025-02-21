package backend.academy.bot.config;

import backend.academy.bot.model.Command;
import backend.academy.bot.repository.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.IClient;
import backend.academy.bot.scrapperClient.MockClient;
import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.telegram.handler.HandlerService;
import backend.academy.bot.telegram.handler.state.CommandHandler;
import backend.academy.bot.telegram.handler.state.CommandHandlerFactory;
import backend.academy.bot.telegram.handler.state.StartCommandHandler;
import backend.academy.bot.telegram.handler.state.TextCommandHandler;
import backend.academy.bot.telegram.handler.state.TrackCommandHandler;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {

    @Bean
    public String telegramToken() {
        return telegramToken;
    }

    @Bean
    public TelegramBotService telegramBot(HandlerService service){
        return new TelegramBotService(telegramToken, service);
    }

    @Bean
    public CommandHandler startCommandHandler(InMemoryTrackingCache userRepository){
        return new StartCommandHandler(userRepository);
    }

    @Bean
    public CommandHandler textCommandHandler(InMemoryTrackingCache userRepository, IClient client){
        return new TextCommandHandler(userRepository, client);
    }

    @Bean
    public CommandHandler trackCommandHandler(InMemoryTrackingCache userRepository){
        return new TrackCommandHandler(userRepository);
    }

    @Bean
    public HandlerService updateHandlerService(CommandHandlerFactory handlerFactory){
        return new HandlerService(handlerFactory);
    }

    @Bean
    public InMemoryTrackingCache userRepository(){
        return new InMemoryTrackingCache();
    }

    @Bean
    public Map<Command, CommandHandler> commandHandlers(InMemoryTrackingCache repository, IClient client) {
        return Map.of(
            Command.START, startCommandHandler(repository),
            Command.TEXT, textCommandHandler(repository, client),
            Command.TRACK, trackCommandHandler(repository)
        );
    }

    @Bean
    public CommandHandlerFactory handlerFactory(Map<Command, CommandHandler> commandHandlers) {
        return new CommandHandlerFactory(commandHandlers);
    }

    @Bean
    public IClient client() {
        return new MockClient();
    }

}
