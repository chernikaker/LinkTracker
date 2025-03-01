package backend.academy.bot.config;


import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.IClient;
import backend.academy.bot.scrapperClient.ScrapperClientService;
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
import backend.academy.bot.telegram.handler.commands.UntrackCommandHandler;
import backend.academy.bot.telegram.handler.commands.UntrackingLinkTextCommandHandler;
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
}
