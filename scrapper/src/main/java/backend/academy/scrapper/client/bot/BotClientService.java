package backend.academy.scrapper.client.bot;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.repository.InMemorySubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/** Сервис для работы с клиентом Bot */
@Service
@Slf4j
public class BotClientService {

    private final BotClient botClient;
    private final InMemorySubscriptionRepository repository;

    public BotClientService(BotClient botClient, InMemorySubscriptionRepository repository) {
        this.botClient = botClient;
        this.repository = repository;
    }

    /** Метод отправления обновлений по ссылке клиентам */
    public void sendUpdates(Link link, List<UpdateInfo> info) {
        // сборка DTO
        LinkUpdate update = makeLinkUpdate(link, info);
        try {
            // отправление сообщения
            botClient.sendUpdates(update);
        } catch (HttpClientErrorException e) {
            // разбор и логирование ошибки
            try {
                ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
                if (error != null) {
                    log.atWarn().addKeyValue("error data", error).log("Error while sending link update");
                } else {
                    log.atWarn().log("Received a null error response. While sending link update");
                }
            } catch (RuntimeException ex) {
                log.atWarn()
                        .addKeyValue("processing error", ex)
                        .log("Error while processing error in sending link update");
            }
        }
    }

    /**
     * Метод формирует DTO из входных данных и данных репозитория о подписках
     *
     * @param link обновленная ссылка
     * @param info список обновлений
     * @return DTO
     */
    private LinkUpdate makeLinkUpdate(Link link, List<UpdateInfo> info) {
        // получение всех подписок на ссылку
        List<Subscription> subscriptionsOnLink = repository.getLinkSubscriptions(link);
        // формирование сообщения об обновлениях
        String message = makeUpdateMessage(info);
        long linkId = subscriptionsOnLink.getFirst().linkId();
        // формирование списка клиентов, кому отправляется сообщение
        List<Long> chatIds = new ArrayList<>();
        for (Subscription subscription : subscriptionsOnLink) {
            chatIds.add(subscription.userId());
        }
        return new LinkUpdate(linkId, link.url(), message, chatIds);
    }

    /**
     * Метод формирует сообщение о всех обновлениях ссылки для пользователя
     *
     * @param info список обновлений
     * @return сообщение
     */
    private String makeUpdateMessage(List<UpdateInfo> info) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < info.size(); i++) {
            sb.append("#").append(i + 1).append('\n');
            sb.append("Тип сообщения: ").append(info.get(i).type().message()).append('\n');
            sb.append("Автор: ").append(info.get(i).authorName()).append('\n');
            sb.append("Время обновления: ").append(info.get(i).time()).append('\n');
            sb.append("Сообщение: ").append(info.get(i).message()).append('\n');
        }
        return sb.toString();
    }
}
