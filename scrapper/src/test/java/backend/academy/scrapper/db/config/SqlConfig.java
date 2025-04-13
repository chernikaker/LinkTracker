package backend.academy.scrapper.db.config;

import backend.academy.scrapper.TestcontainersConfig;
import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.db.sql.repository.FilterSqlRepository;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.TagSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.db.sql.service.SqlFilterService;
import backend.academy.scrapper.db.sql.service.SqlLinkService;
import backend.academy.scrapper.db.sql.service.SqlSubscriptionService;
import backend.academy.scrapper.db.sql.service.SqlTagService;
import backend.academy.scrapper.db.sql.service.SqlUserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
@ComponentScan(basePackages = "backend.academy.scrapper.db.sql.repository")
@Import(TestcontainersConfig.class)
public class SqlConfig {

    @Bean
    @Primary
    public UserService userService(
            UserSqlRepository userRepo, SubscriptionSqlRepository subscriptionRepo, LinkSqlRepository linkRepo) {
        return new SqlUserService(userRepo, subscriptionRepo, linkRepo);
    }

    @Bean
    @Primary
    public SubscriptionService subscriptionService(
            UserSqlRepository userRepo,
            SubscriptionSqlRepository subscriptionRepo,
            LinkSqlRepository linkRepo,
            TagSqlRepository tagRepo,
            FilterSqlRepository filterRepo) {
        return new SqlSubscriptionService(userRepo, linkRepo, subscriptionRepo, tagRepo, filterRepo);
    }

    @Bean
    @Primary
    public LinkService linkService(LinkSqlRepository linkRepo) {
        return new SqlLinkService(linkRepo);
    }

    @Bean
    @Primary
    public TagService tagService(
            UserSqlRepository userRepo,
            SubscriptionSqlRepository subscriptionRepo,
            LinkSqlRepository linkRepo,
            TagSqlRepository tagRepo) {
        return new SqlTagService(userRepo, linkRepo, subscriptionRepo, tagRepo);
    }

    @Bean
    @Primary
    public FilterService filterService(FilterSqlRepository filterRepo) {
        return new SqlFilterService(filterRepo);
    }
}
