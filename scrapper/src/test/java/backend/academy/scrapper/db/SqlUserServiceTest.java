package backend.academy.scrapper.db;

import backend.academy.scrapper.db.config.SqlConfig;
import backend.academy.scrapper.db.integration.UserServiceTest;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(SqlConfig.class)
@JdbcTest
@Testcontainers
public class SqlUserServiceTest extends UserServiceTest {

}
