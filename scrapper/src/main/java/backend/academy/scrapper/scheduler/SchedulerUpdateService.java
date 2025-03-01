package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.dto.GithubInfo;
import backend.academy.scrapper.client.github.GithubClient;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerUpdateService {

    private final InMemoryLinkRepository linkRepository;
    private final GithubClientService githubClientService;
    private final BotClientService botClientService;

    public SchedulerUpdateService(
        InMemoryLinkRepository linkRepository,
        GithubClientService githubClientService,
        BotClientService service
    ) {
        this.linkRepository = linkRepository;
        this.githubClientService = githubClientService;
        this.botClientService = service;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void checkNewUpdates(){
        Set<Link> links = linkRepository.getLinks();
        for (Link link : links) {
            List<GithubInfo> updates = githubClientService.getAllInfo(link);
            List<GithubInfo> actualCommits = updates
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
