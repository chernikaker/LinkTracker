package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.GithubResponseJsonIsInvalid;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.exception.client.StackoverflowResponseJsonIsInvalid;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Сервис для периодической проверки обновлений Взаимодействует с сервисами внешних клиентов для получения обновлений,
 * сервисом клиента бота для их отправки, репозиторием для получения и обновления информации ссылок
 */
@Service
@AllArgsConstructor
@Slf4j
public class SchedulerUpdateService {

    public static final int INITIAL_DELAY = 10000;
    public static final int DELAY = 10000;

    private final InMemoryLinkRepository linkRepository;
    private final GithubClientService githubClientService;
    private final StackoverflowClientService soClientService;
    private final BotClientService botClientService;

    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = DELAY)
    public void checkNewUpdates() {
        log.debug("Scheduling checking link updates");
        Set<Link> links = linkRepository.getLinks();
        for (Link link : links) {
            try {
                // получение всех обновлений
                List<UpdateInfo> updates = link.type() == LinkType.GITHUB
                        ? githubClientService.getAllInfo(link)
                        : soClientService.getAllInfo(link);
                // фильтрация новых обновлений по дате последней проверки
                List<UpdateInfo> actualInfos = updates.stream()
                        .filter(info -> link.lastValidation().isBefore(info.time()))
                        .toList();
                // если есть новые обновления, отправляем их пользователю
                if (!actualInfos.isEmpty()) {
                    botClientService.sendUpdates(link, actualInfos);
                }
            } catch (ScrapperInternalResponseException
                    | GithubResponseJsonIsInvalid
                    | StackoverflowResponseJsonIsInvalid e) {
                log.error("Error while getting update {}", e.getMessage());
            }
            // обновление времени проверки ссылки
            link.lastValidation(LocalDateTime.now(ZoneId.systemDefault()));
        }
    }
}
