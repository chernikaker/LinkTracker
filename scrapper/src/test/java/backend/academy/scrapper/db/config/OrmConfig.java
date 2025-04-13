package backend.academy.scrapper.db.config;

import backend.academy.scrapper.TestcontainersConfig;
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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "backend.academy.scrapper.db.orm.repository")
@EntityScan(basePackages = "backend.academy.scrapper.db.orm.entity")
@Import(TestcontainersConfig.class)
public class OrmConfig {

    @Bean
    @Primary
    public UserService userService(OrmUserRepository userRepo, OrmLinkRepository linkRepo) {
        return new OrmUserService(userRepo, linkRepo);
    }

    @Bean
    @Primary
    public SubscriptionService subscriptionService(
            OrmUserRepository userRepo,
            OrmLinkRepository linkRepo,
            OrmSubscriptionRepository subscriptionRepo,
            OrmTagRepository tagRepo) {
        return new OrmSubscriptionService(subscriptionRepo, linkRepo, userRepo, tagRepo);
    }

    @Bean
    @Primary
    public LinkService linkService(OrmLinkRepository linkRepo) {
        return new OrmLinkService(linkRepo);
    }

    @Bean
    @Primary
    public TagService tagService(
            OrmUserRepository userRepo, OrmSubscriptionRepository subscriptionRepo, OrmTagRepository tagRepo) {
        return new OrmTagService(userRepo, subscriptionRepo, tagRepo);
    }

    @Bean
    @Primary
    public FilterService filterService(OrmFilterRepository filterRepo) {
        return new OrmFilterService(filterRepo);
    }
}
