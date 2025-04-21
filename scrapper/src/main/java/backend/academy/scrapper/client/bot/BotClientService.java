package backend.academy.scrapper.client.bot;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.LinkUpdateUnit;
import backend.academy.scrapper.db.contract.SubscriptionService;
import backend.academy.scrapper.db.contract.TagService;
import backend.academy.scrapper.entity.Subscription;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.model.UpdateInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final TagService tagService;

    /** Метод отправления обновлений по ссылке клиентам */
    public void sendUpdates(long linkId, String url, List<UpdateInfo> info) {
        // сборка DTO
        LinkUpdate update = makeLinkUpdate(linkId, url, info);
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
     * @param url обновленная ссылка
     * @param info список обновлений
     * @return DTO
     */
    private LinkUpdate makeLinkUpdate(long linkId, String url, List<UpdateInfo> info) {
        // получение всех подписок на ссылку
        List<Subscription> dbChatsInfo = service.getSubscriptionsByLinkId(linkId);
        // информация о чатах подписчиков и списке тегов для данной ссылки
        Map<Long, List<String>> chatsWithTags = processDbInfo(dbChatsInfo);
        // формирование сообщения об обновлениях
        List<LinkUpdateUnit> updateUnits = makeUpdateUnits(info);
        return new LinkUpdate(linkId, url, updateUnits, chatsWithTags);
    }

    private Map<Long, List<String>> processDbInfo(List<Subscription> dbChatsInfo) {
        Map<Long, List<String>> chatsWithTags = new HashMap<>();
        for (Subscription info : dbChatsInfo) {
            List<String> tags = info.tags().stream().map(Tag::value).toList();
            chatsWithTags.put(info.user().chatId(), tags);
        }
        return chatsWithTags;
    }

    /**
     * Метод формирует список DTO об обновлениях ссылки для пользователя
     *
     * @param info список обновлений
     * @return список соответствующих DTO
     */
    private List<LinkUpdateUnit> makeUpdateUnits(List<UpdateInfo> info) {
        return info.stream()
                .map(i -> new LinkUpdateUnit(
                        i.title(),
                        i.message(),
                        i.time(),
                        i.authorName(),
                        i.type().message()))
                .toList();
    }
}
