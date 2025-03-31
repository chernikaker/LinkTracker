package backend.academy.scrapper.client.bot;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.model.UpdateInfo;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/** Сервис для работы с клиентом Bot */
@Service
@Slf4j
@AllArgsConstructor
public class BotClientService {

    private final BotClient botClient;
    private final SubscriptionService service;

    /** Метод отправления обновлений по ссылке клиентам */
    public void sendUpdates(long linkId, Link link, List<UpdateInfo> info) {
        // сборка DTO
        LinkUpdate update = makeLinkUpdate(linkId, link, info);
        try {
            // отправление сообщения
            botClient.sendUpdates(update);
        } catch (HttpClientErrorException e) {
            // разбор и логирование ошибки
            try {
                ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
                if (error != null) {
                    System.out.println(error.exceptionMessage());
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
    private LinkUpdate makeLinkUpdate(long linkId, Link link, List<UpdateInfo> info) {
        // получение всех подписчиков на ссылку
        List<Long> subscriberChats = service.getLinkSubscribersChatsById(linkId);
        // формирование сообщения об обновлениях
        List<LinkUpdateUnit> updateUnits = new ArrayList<>();
        return new LinkUpdate(linkId, link.url(), updateUnits, subscriberChats);
    }

    /**
     * Метод формирует список DTO об обновлениях ссылки для пользователя
     *
     * @param info список обновлений
     * @return список соответствующих DTO
     */
    private List<LinkUpdateUnit> makeUpdateUnits(List<UpdateInfo> info) {
        return info.stream()
            .map(i -> new LinkUpdateUnit(i.title(), i.message(), i.time(), i.authorName(), i.type().message()))
            .toList();
    }
}
