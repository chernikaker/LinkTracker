package backend.academy.scrapper;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import lombok.experimental.UtilityClass;
import org.testcontainers.containers.PostgreSQLContainer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

@UtilityClass
public class MigrationsApplier {

    private final static Path MIGRATIONS_PATH = Paths.get("../migrations");

    public static void applyMigrations(PostgreSQLContainer<?> postgres) {
        try (Connection connection = DriverManager.getConnection(
            postgres.getJdbcUrl(),
            postgres.getUsername(),
            postgres.getPassword())) {
            var database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            var resourceAccessor = new DirectoryResourceAccessor(MIGRATIONS_PATH);
            try (Liquibase liquibase = new Liquibase("master.xml", resourceAccessor, database)) {
                liquibase.update(new Contexts());
            } catch (LiquibaseException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
