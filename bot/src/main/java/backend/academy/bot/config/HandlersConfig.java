package backend.academy.bot.config;

import backend.academy.bot.cache.InMemoryTrackingCache;
import backend.academy.bot.scrapperClient.ScrapperClientService;
import backend.academy.bot.telegram.handler.commands.CommandHandler;
import backend.academy.bot.telegram.handler.commands.FiltersTextHandler;
import backend.academy.bot.telegram.handler.commands.HelpHandler;
import backend.academy.bot.telegram.handler.commands.LinkTextHandler;
import backend.academy.bot.telegram.handler.commands.ListByTagHandler;
import backend.academy.bot.telegram.handler.commands.ListHandler;
import backend.academy.bot.telegram.handler.commands.RemoveTagFromSubHandler;
import backend.academy.bot.telegram.handler.commands.RemoveTagHandler;
import backend.academy.bot.telegram.handler.commands.StartHandler;
import backend.academy.bot.telegram.handler.commands.TagListHandler;
import backend.academy.bot.telegram.handler.commands.TagsTextHandler;
import backend.academy.bot.telegram.handler.commands.TagsToSubHandler;
import backend.academy.bot.telegram.handler.commands.TrackHandler;
import backend.academy.bot.telegram.handler.commands.UnknownCommandHandler;
import backend.academy.bot.telegram.handler.commands.UntrackByTagHandler;
import backend.academy.bot.telegram.handler.commands.UntrackHandler;
import backend.academy.bot.telegram.handler.sender.tag_text.TagCommandSenderFactory;
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
        return new StartHandler(userRepository, scrapperClientService);
    }

    @Bean
    @Order(2)
    public CommandHandler listCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new ListHandler(userRepository, service);
    }

    @Bean
    @Order(3)
    public CommandHandler trackCommandHandler(InMemoryTrackingCache userRepository) {
        return new TrackHandler(userRepository);
    }

    @Bean
    @Order(4)
    public CommandHandler helpCommandHandler(InMemoryTrackingCache userRepository) {
        return new HelpHandler(userRepository);
    }

    @Bean
    @Order(5)
    public CommandHandler untrackCommandHandler(InMemoryTrackingCache userRepository) {
        return new UntrackHandler(userRepository);
    }

    @Bean
    @Order(6)
    public CommandHandler removeTagHandler(InMemoryTrackingCache userRepository) {
        return new RemoveTagHandler(userRepository);
    }

    @Bean
    @Order(7)
    public CommandHandler removeTagFromSubHandler(InMemoryTrackingCache userRepository) {
        return new RemoveTagFromSubHandler(userRepository);
    }

    @Bean
    @Order(8)
    public CommandHandler tagListHandler(InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new TagListHandler(userRepository, service);
    }

    @Bean
    @Order(9)
    public CommandHandler addTagsToSubHandler(InMemoryTrackingCache userRepository) {
        return new TagsToSubHandler(userRepository);
    }

    @Bean
    @Order(10)
    public CommandHandler untrackByTagHandler(InMemoryTrackingCache userRepository) {
        return new UntrackByTagHandler(userRepository);
    }

    @Bean
    @Order(11)
    public CommandHandler listByTagHandler(InMemoryTrackingCache userRepository) {
        return new ListByTagHandler(userRepository);
    }

    @Bean
    @Order(12)
    public CommandHandler linkTextCommandHandler(InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new LinkTextHandler(userRepository, service);
    }

    @Bean
    @Order(13)
    public CommandHandler tagsTextCommandHandler(
            InMemoryTrackingCache userRepository, TagCommandSenderFactory factory) {
        return new TagsTextHandler(userRepository, factory);
    }

    @Bean
    @Order(14)
    public CommandHandler filtersTextCommandHandler(
            InMemoryTrackingCache userRepository, ScrapperClientService service) {
        return new FiltersTextHandler(userRepository, service);
    }

    @Bean
    @Order(15)
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
    public List<CommandHandler> commandHandlers(
            InMemoryTrackingCache cache, ScrapperClientService service, TagCommandSenderFactory factory) {
        List<CommandHandler> list = new ArrayList<>(List.of(
                startCommandHandler(cache, service),
                trackCommandHandler(cache),
                helpCommandHandler(cache),
                listCommandHandler(cache, service),
                untrackCommandHandler(cache),
                linkTextCommandHandler(cache, service),
                tagsTextCommandHandler(cache, factory),
                filtersTextCommandHandler(cache, service),
                unknownCommandHandler(cache),
                removeTagHandler(cache),
                removeTagFromSubHandler(cache),
                tagListHandler(cache, service),
                addTagsToSubHandler(cache),
                untrackCommandHandler(cache),
                listByTagHandler(cache)));
        list.sort(AnnotationAwareOrderComparator.INSTANCE);
        return list;
    }
}
