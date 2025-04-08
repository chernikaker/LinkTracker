package backend.academy.scrapper.db.config;

import backend.academy.scrapper.TestcontainersConfig;
import backend.academy.scrapper.db.sql.service.SqlFilterService;
import backend.academy.scrapper.db.sql.service.SqlLinkService;
import backend.academy.scrapper.db.sql.service.SqlSubscriptionService;
import backend.academy.scrapper.db.sql.service.SqlTagService;
import backend.academy.scrapper.db.sql.service.SqlUserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@ComponentScan(basePackages = "backend.academy.scrapper.db.sql.repository")
@Import({
    TestcontainersConfig.class,
    SqlUserService.class,
    SqlSubscriptionService.class,
    SqlLinkService.class,
    SqlFilterService.class,
    SqlTagService.class
})
public class SqlConfig {
}
