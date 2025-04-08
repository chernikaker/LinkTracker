package backend.academy.scrapper.db.config;

import backend.academy.scrapper.TestcontainersConfig;
import backend.academy.scrapper.db.orm.service.OrmFilterService;
import backend.academy.scrapper.db.orm.service.OrmLinkService;
import backend.academy.scrapper.db.orm.service.OrmSubscriptionService;
import backend.academy.scrapper.db.orm.service.OrmTagService;
import backend.academy.scrapper.db.orm.service.OrmUserService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;

@TestConfiguration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "backend.academy.scrapper.db.orm.repository")
@EntityScan(basePackages = "backend.academy.scrapper.db.orm.entity")
@Import({
    TestcontainersConfig.class,
    OrmUserService.class,
    OrmSubscriptionService.class,
    OrmLinkService.class,
    OrmFilterService.class,
    OrmTagService.class
})
public class OrmConfig {}
