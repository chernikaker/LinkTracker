package backend.academy.bot.config;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import backend.academy.bot.telegram.handler.commands.FiltersTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.HelpCommandHandler;
import backend.academy.bot.telegram.handler.commands.LinkTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.ListCommandHandler;
import backend.academy.bot.telegram.handler.commands.StartCommandHandler;
import backend.academy.bot.telegram.handler.commands.TagsTextCommandHandler;
import backend.academy.bot.telegram.handler.commands.TrackCommandHandler;
import backend.academy.bot.telegram.handler.commands.UnknownCommandHandler;
import backend.academy.bot.telegram.handler.commands.UntrackCommandHandler;
import backend.academy.bot.telegram.handler.commands.UntrackingLinkTextCommandHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class HandlersConfig {

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
    public CommandHandler untrackCommandHandler(InMemoryTrackingCache userRepository){
        return new UntrackCommandHandler(userRepository);
    }

    @Bean
    public CommandHandler untrackLinkTextCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService service){
        return new UntrackingLinkTextCommandHandler(userRepository, service);
    }

    @Bean
    public List<CommandHandler> commandHandlers(InMemoryTrackingCache repository, ScrapperClientService service) {
        return List.of(
            startCommandHandler(repository, service),
            trackCommandHandler(repository),
            helpCommandHandler(repository),
            listCommandHandler(repository, service),
            untrackCommandHandler(repository),
            linkTextCommandHandler(repository),
            tagsTextCommandHandler(repository),
            filtersTextCommandHandler(repository, service),
            untrackLinkTextCommandHandler(repository, service),
            unknownCommandHandler(repository)
        );
    }
}
