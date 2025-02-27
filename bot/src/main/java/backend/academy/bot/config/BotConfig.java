package backend.academy.bot.config;


import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.IClient;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.scrapperClient.ScrapperRestClient;
//import backend.academy.bot.server.BotService;
//import backend.academy.bot.server.BotServiceImpl;
import backend.academy.bot.telegram.TelegramBotService;
import backend.academy.bot.telegram.handler.HandlerService;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import backend.academy.bot.telegram.handler.commands.FiltersTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.HelpCommandHandler;
import backend.academy.bot.telegram.handler.commands.LinkTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.ListCommandHandler;
import backend.academy.bot.telegram.handler.commands.StartCommandHandler;
import backend.academy.bot.telegram.handler.commands.TagsTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.TrackCommandHandler;
import backend.academy.bot.telegram.handler.commands.UnknownCommandHandler;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
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
    public ScrapperClientService scrapperClientService(IClient client){
        return new ScrapperClientService(client);
    }

    @Bean
    public CommandHandler startCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService scrapperClientService){
        return new StartCommandHandler(userRepository, scrapperClientService);
    }

    @Bean
    public CommandHandler linkTextCommandHandler(InMemoryTrackingCache userRepository){
        return new LinkTextCommandHandler(userRepository);
    }

    @Bean
    public CommandHandler tagsTextCommandHandler(InMemoryTrackingCache userRepository){
        return new TagsTextCommandHandler(userRepository);
    }

    @Bean
    public CommandHandler filtersTextCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService service){
        return new FiltersTextCommandHandler(userRepository, service);
    }

    @Bean
    public CommandHandler listCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService service){
        return new ListCommandHandler(userRepository, service);
    }


    @Bean
    public CommandHandler trackCommandHandler(InMemoryTrackingCache userRepository){
        return new TrackCommandHandler(userRepository);
    }

    @Bean
    public CommandHandler helpCommandHandler(InMemoryTrackingCache userRepository){
        return new HelpCommandHandler(userRepository);
    }

    @Bean
    public CommandHandler unknownCommandHandler(InMemoryTrackingCache userRepository){
        return new UnknownCommandHandler(userRepository);
    }

    @Bean
    public HandlerService updateHandlerService(List<CommandHandler> commandHandlers){
        return new HandlerService(commandHandlers);
    }

    @Bean
    public InMemoryTrackingCache userRepository(){
        return new InMemoryTrackingCache();
    }

    @Bean
    public List<CommandHandler> commandHandlers(InMemoryTrackingCache repository, ScrapperClientService service) {
        return List.of(
            startCommandHandler(repository, service),
            trackCommandHandler(repository),
            helpCommandHandler(repository),
            linkTextCommandHandler(repository),
            listCommandHandler(repository, service),
            tagsTextCommandHandler(repository),
            filtersTextCommandHandler(repository, service),
            unknownCommandHandler(repository)
        );
    }


    @Bean
    public IClient client() {
        return new ScrapperRestClient("http://localhost:8081/");
    }

}
