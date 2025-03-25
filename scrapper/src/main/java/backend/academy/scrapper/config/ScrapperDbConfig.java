package backend.academy.scrapper.config;

import backend.academy.scrapper.db.AdditionalInfoService;
import backend.academy.scrapper.db.SubscriptionService;
import backend.academy.scrapper.db.LinkService;
import backend.academy.scrapper.db.UserService;
import backend.academy.scrapper.db.sql.SqlAdditionalInfoService;
import backend.academy.scrapper.db.sql.SqlSubscriptionService;
import backend.academy.scrapper.db.sql.SqlLinkService;
import backend.academy.scrapper.db.sql.SqlUserService;
import backend.academy.scrapper.db.sql.repository.FilterSqlRepository;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.TagSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrapperDbConfig {

    @Bean
    public SubscriptionService sqlDbService(UserSqlRepository userRepo,
                                            LinkSqlRepository linkRepo,
                                            SubscriptionSqlRepository subscrRepo,
                                            TagSqlRepository tagRepo,
                                            FilterSqlRepository filterRepo){
        return new SqlSubscriptionService(userRepo, linkRepo, subscrRepo, tagRepo, filterRepo);
    }

    @Bean
    public LinkService sqlLinkDbService(LinkSqlRepository linkRepo){
        return new SqlLinkService(linkRepo);
    }

    @Bean
    public UserService sqlUserDbService(UserSqlRepository userRepo,
                                        LinkSqlRepository linkRepo,
                                        SubscriptionSqlRepository subscrRepo){
        return new SqlUserService(userRepo, subscrRepo, linkRepo);
    }

    @Bean
    public AdditionalInfoService sqlAdditionalInfoDbService(UserSqlRepository userRepo,
                                                            LinkSqlRepository linkRepo,
                                                            SubscriptionSqlRepository subscrRepo,
                                                            TagSqlRepository tagRepo,
                                                            FilterSqlRepository filterRepo){
        return new SqlAdditionalInfoService(userRepo, linkRepo, subscrRepo, tagRepo, filterRepo) {
        };
    }
}
