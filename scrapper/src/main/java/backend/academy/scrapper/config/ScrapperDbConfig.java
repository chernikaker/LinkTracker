package backend.academy.scrapper.config;

import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.db.orm.repository.OrmFilterRepository;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.db.orm.service.OrmFilterService;
import backend.academy.scrapper.db.orm.service.OrmLinkService;
import backend.academy.scrapper.db.orm.service.OrmSubscriptionService;
import backend.academy.scrapper.db.orm.service.OrmTagService;
import backend.academy.scrapper.db.orm.service.OrmUserService;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrapperDbConfig {

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "sql")
    public SubscriptionService sqlSubscriptionDbService(
            UserSqlRepository userRepo,
            LinkSqlRepository linkRepo,
            SubscriptionSqlRepository subscrRepo,
            TagSqlRepository tagRepo,
            FilterSqlRepository filterRepo) {
        return new SqlSubscriptionService(userRepo, linkRepo, subscrRepo, tagRepo, filterRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "sql")
    public LinkService sqlLinkDbService(LinkSqlRepository linkRepo) {
        return new SqlLinkService(linkRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "sql")
    public UserService sqlUserDbService(
            UserSqlRepository userRepo, LinkSqlRepository linkRepo, SubscriptionSqlRepository subscrRepo) {
        return new SqlUserService(userRepo, subscrRepo, linkRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "sql")
    public TagService sqlTagDbService(
            UserSqlRepository userRepo,
            LinkSqlRepository linkRepo,
            SubscriptionSqlRepository subscrRepo,
            TagSqlRepository tagRepo) {
        return new SqlTagService(userRepo, linkRepo, subscrRepo, tagRepo) {};
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "sql")
    public FilterService sqlFilterDbService(FilterSqlRepository filterRepo) {
        return new SqlFilterService(filterRepo) {};
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "orm")
    public SubscriptionService ormSubscriptionDbService(
            OrmUserRepository userRepo,
            OrmSubscriptionRepository subscrRepo,
            OrmTagRepository tagRepo,
            OrmLinkRepository linkRepo) {
        return new OrmSubscriptionService(subscrRepo, linkRepo, userRepo, tagRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "orm")
    public UserService ormUserDbService(OrmUserRepository userRepo, OrmLinkRepository linkRepo) {
        return new OrmUserService(userRepo, linkRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "orm")
    public LinkService ormLinkDbService(OrmLinkRepository linkRepo) {
        return new OrmLinkService(linkRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "orm")
    public TagService ormTagDbService(
            OrmUserRepository userRepo, OrmSubscriptionRepository subscrRepo, OrmTagRepository tagRepo) {
        return new OrmTagService(userRepo, subscrRepo, tagRepo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db", name = "access", havingValue = "orm")
    public FilterService ormFilterDbService(OrmFilterRepository filterRepo) {
        return new OrmFilterService(filterRepo);
    }
}
