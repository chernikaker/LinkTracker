package backend.academy.scrapper.config;

import backend.academy.scrapper.db.DatabaseService;
import backend.academy.scrapper.db.sql.SqlDatabaseService;
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
    public DatabaseService sqlDbService(UserSqlRepository userRepo,
                                        LinkSqlRepository linkRepo,
                                        SubscriptionSqlRepository subscrRepo,
                                        TagSqlRepository tagRepo,
                                        FilterSqlRepository filterRepo){
        return new SqlDatabaseService(userRepo, linkRepo, subscrRepo, tagRepo, filterRepo);
    }
}
