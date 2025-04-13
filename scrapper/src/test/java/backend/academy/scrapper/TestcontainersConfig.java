package backend.academy.scrapper;

import static backend.academy.scrapper.MigrationsApplier.applyMigrations;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        var postgres = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("link_update_test")
                .withUsername("postgres")
                .withPassword("test")
                .withExposedPorts(5432);
        postgres.start();
        applyMigrations(postgres);
        return postgres;
    }
}
