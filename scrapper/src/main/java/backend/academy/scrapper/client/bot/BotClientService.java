package backend.academy.scrapper.client.bot;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.client.github.dto.GithubInfo;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BotClientService {

    private final BotClient botClient;
    private final InMemorySubscriptionRepository repository;

    public BotClientService(BotClient botClient, InMemorySubscriptionRepository repository) {
        this.botClient = botClient;
        this.repository = repository;
    }

    public void sendUpdates(Link link, List<GithubInfo> info) {
        List<Subscription> subscriptionsOnLink = repository.getLinkSubscriptions(link);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<info.size(); i++) {
            sb.append("#").append(i+1).append('\n');
            sb.append("Тип сообщения: ").append(info.get(i).type().message()).append('\n');
            sb.append("Автор: ").append(info.get(i).authorName()).append('\n');
            sb.append("Время обновления: ").append(info.get(i).time()).append('\n');
            sb.append("Сообщение: ").append(info.get(i).message()).append('\n');
        }
        long linkId = subscriptionsOnLink.getFirst().linkId();
        List<Long> chatIds = new ArrayList<>();
        for(Subscription subscription : subscriptionsOnLink) {
            chatIds.add(subscription.userId());
        }
        LinkUpdate update = new LinkUpdate(linkId, link.url(), sb.toString(), chatIds);
        try {
            botClient.sendUpdates(update);
        } catch (HttpClientErrorException e) {
            ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
            log.error("Error while sending link update: {}", error.exceptionMessage());
        }
    }
}
