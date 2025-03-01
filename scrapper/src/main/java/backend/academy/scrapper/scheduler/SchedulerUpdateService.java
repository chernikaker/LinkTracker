package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.dto.UpdateInfo;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchedulerUpdateService {

    private final InMemoryLinkRepository linkRepository;
    private final GithubClientService githubClientService;
    private final StackoverflowClientService soClientService;
    private final BotClientService botClientService;

    public SchedulerUpdateService(
        InMemoryLinkRepository linkRepository,
        GithubClientService githubClientService,
        BotClientService service,
        StackoverflowClientService soClientService
    ) {
        this.linkRepository = linkRepository;
        this.githubClientService = githubClientService;
        this.botClientService = service;
        this.soClientService = soClientService;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void checkNewUpdates(){
        log.debug("Scheduling checking link updates");
        Set<Link> links = linkRepository.getLinks();
        for (Link link : links) {
            List<UpdateInfo> updates =
                link.type() == LinkType.GITHUB
                    ? githubClientService.getAllInfo(link)
                    : soClientService.getAllInfo(link);
            List<UpdateInfo> actualCommits = updates
                .stream()
                .filter(info -> link.lastValidation().isBefore(info.time()))
                .toList();
            if(!actualCommits.isEmpty()){
                botClientService.sendUpdates(link, actualCommits);
            }
            link.lastValidation(LocalDateTime.now());
        }
    }
}
