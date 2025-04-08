package backend.academy.scrapper.db;

import backend.academy.scrapper.db.config.OrmConfig;
import backend.academy.scrapper.db.integration.UserServiceTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(OrmConfig.class)
@DataJpaTest
@Testcontainers
public class OrmUserServiceTest extends UserServiceTest {

}
