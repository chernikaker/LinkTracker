package backend.academy.scrapper;

import backend.academy.scrapper.config.ScrapperPropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ScrapperPropertiesConfig.class)
public class ScrapperApplication {

    /**
     * Главный метод для запуска приложения.
     *
     * @param args аргументы командной строки.
     */
    public static void main(final String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
