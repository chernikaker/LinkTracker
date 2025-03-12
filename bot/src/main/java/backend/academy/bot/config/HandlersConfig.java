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
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;

/** Класс Spring когнигурации для обработчиков ввода пользователя в телеграм-бот */
@Configuration
public class HandlersConfig {

    @Bean
    @Order(1)
    public CommandHandler startCommandHandler(
            InMemoryTrackingCache userRepository, ScrapperClientService scrapperClientService) {
        return new StartCommandHandler(userRepository, scrapperClientService);
    }

    @Bean
    @Order(6)
    public CommandHandler linkTextCommandHandler(InMemoryTrackingCache userRepository) {
        return new LinkTextCommandHandler(userRepository);
    }

    @Bean
    @Order(7)
    public CommandHandler tagsTextCommandHandler(InMemoryTrackingCache userRepository) {
        return new TagsTextCommandHandler(userRepository);
    }

    @Bean
    @Order(8)
    public CommandHandler filtersTextCommandHandler(
            InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new FiltersTextCommandHandler(userRepository, service);
    }

    @Bean
    @Order(2)
    public CommandHandler listCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new ListCommandHandler(userRepository, service);
    }

    @Bean
    @Order(3)
    public CommandHandler trackCommandHandler(InMemoryTrackingCache userRepository) {
        return new TrackCommandHandler(userRepository);
    }

    @Bean
    @Order(4)
    public CommandHandler helpCommandHandler(InMemoryTrackingCache userRepository) {
        return new HelpCommandHandler(userRepository);
    }

    @Bean
    @Order(5)
    public CommandHandler untrackCommandHandler(InMemoryTrackingCache userRepository) {
        return new UntrackCommandHandler(userRepository);
    }

    @Bean
    @Order(9)
    public CommandHandler untrackLinkTextCommandHandler(
            InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new UntrackingLinkTextCommandHandler(userRepository, service);
    }

    @Bean
    @Order(10)
    // максимальное значение Order должно быть у данного обработчика
    public CommandHandler unknownCommandHandler(InMemoryTrackingCache userRepository) {
        return new UnknownCommandHandler(userRepository);
    }
    /**
     * Метод, определяющий, каким образом Spring инжектит список обработчиков в соответствующий сервис. Список
     * сортируется по Order, обеспечивая приоритет выбора обработчиков
     *
     * @param cache - кэш для временной информации
     * @param service - сервис клиента для взаимодействия со Scrapper
     * @return список обработчиков команд
     */
    @Bean
    public List<CommandHandler> commandHandlers(InMemoryTrackingCache cache, ScrapperClientService service) {
        List<CommandHandler> list = new ArrayList<>(List.of(
                startCommandHandler(cache, service),
                trackCommandHandler(cache),
                helpCommandHandler(cache),
                listCommandHandler(cache, service),
                untrackCommandHandler(cache),
                linkTextCommandHandler(cache),
                tagsTextCommandHandler(cache),
                filtersTextCommandHandler(cache, service),
                untrackLinkTextCommandHandler(cache, service),
                unknownCommandHandler(cache)));
        list.sort(AnnotationAwareOrderComparator.INSTANCE);
        return list;
    }
}
