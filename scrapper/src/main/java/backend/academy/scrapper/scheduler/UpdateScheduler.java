package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.db.LinkService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.client.ScrapperInternalResponseException;
import backend.academy.scrapper.model.UpdateInfo;
import java.util.List;
import java.util.Map;
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
public class UpdateScheduler {

    public static final int INITIAL_DELAY = 10000;
    public static final int DELAY = 10000;
    public static final int batchSize = 100;
    public static final long notCheckedIntervalSeconds = 10;

    private final LinkService linkDbService;
    private final GithubClientService githubClientService;
    private final StackoverflowClientService soClientService;
    private final BotClientService botClientService;

    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = DELAY)
    public void checkNewUpdates() {
        log.atDebug().log("Scheduling checking link updates");
        long offset = 0;
        Map<Long, Link> links;
        do {
            links = linkDbService.getLinksToCheck(batchSize, offset, notCheckedIntervalSeconds);
            for (Map.Entry<Long,Link> linkData : links.entrySet()) {
                try {
                    Link link = linkData.getValue();
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
                } catch (ScrapperInternalResponseException e) {
                    log.atWarn().setCause(e).log("Error while getting update in scheduler");
                }
                // обновление времени проверки ссылки
                linkDbService.updateLinkValidationOnCurrentTime(linkData.getKey());
                offset += batchSize;
            }
        } while (!links.isEmpty());
    }
}
