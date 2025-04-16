package backend.academy.scrapper.service;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkTagResponse;
import backend.academy.dto.ListLinkTagsResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.dto.AddLinkTagsRequest;
import backend.academy.dto.RemoveLinkTagRequest;
import backend.academy.dto.RemoveTagRequest;
import backend.academy.dto.TagResponse;
import backend.academy.scrapper.exception.controller.ScrapperControllerEntityNotFoundException;
import backend.academy.scrapper.exception.controller.ScrapperInvalidIdException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Главный контроллер сервера Scrapper */
@RestController
@AllArgsConstructor
public class ScrapperController {

    private final ScrapperService scrapperService;

    /**
     * Регистрирует новый Telegram чат.
     *
     * @param id идентификатор Telegram чата, который необходимо зарегистрировать.
     * @return ResponseEntity с кодом 200.
     * @throws ScrapperInvalidIdException если идентификатор не положительный.
     */
    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<?> registerChat(@PathVariable final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        scrapperService.registerUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет существующий Telegram чат.
     *
     * @param id идентификатор Telegram чата, который необходимо удалить.
     * @return ResponseEntity с кодом 200.
     * @throws ScrapperInvalidIdException если идентификатор не положительный.
     * @throws ScrapperControllerEntityNotFoundException если чат с данным идентификатором не существует (код 404).
     */
    @DeleteMapping("/tg-chat/{id}")
    public final ResponseEntity<?> deleteChat(@PathVariable final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        try {
            scrapperService.deleteUser(id);
        } catch (ScrapperUserNotExistsException ex) {
            throw new ScrapperControllerEntityNotFoundException(ex.getMessage(), ex);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Получает список ссылок для указанного Telegram чата.
     *
     * @param id идентификатор Telegram чата, для которого необходимо получить ссылки.
     * @return ResponseEntity с кодом 200 и списком ссылок в теле ответа.
     * @throws ScrapperInvalidIdException если идентификатор не положительный.
     */
    @GetMapping("/links")
    public final ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        ListLinksResponse response = scrapperService.getUserLinks(id);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    /**
     * Добавляет подписку на ссылку для указанного Telegram чата.
     *
     * @param id идентификатор Telegram чата, для которого необходимо добавить подписку.
     * @param request объект запроса, содержащий данные для добавления подписки.
     * @return ResponseEntity с кодом 200 и информацией о добавленной подписке в теле ответа.
     * @throws ScrapperInvalidIdException если идентификатор не положительный.
     */
    @PostMapping("/links")
    public final ResponseEntity<?> addLinkSubscription(
            @RequestHeader("Tg-Chat-Id") final Long id, @RequestBody @Valid final AddLinkRequest request) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        LinkResponse response = scrapperService.addSubscription(id, request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    /**
     * Удаляет подписку на ссылку для указанного Telegram чата.
     *
     * @param id идентификатор Telegram чата, для которого необходимо удалить подписку.
     * @param request объект запроса, содержащий данные для удаления подписки.
     * @return ResponseEntity с кодом 200 и информацией о удалённой подписке в теле ответа.
     * @throws ScrapperInvalidIdException если идентификатор не положительный.
     * @throws ScrapperControllerEntityNotFoundException если подписка или ссылка не найдены (код 404).
     */
    @DeleteMapping("/links")
    public final ResponseEntity<?> deleteLinkSubscription(
            @RequestHeader("Tg-Chat-Id") final Long id, @RequestBody @Valid final RemoveLinkRequest request) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        try {
            LinkResponse response = scrapperService.deleteSubscription(id, request);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (ScrapperLinkNotExistsException | ScrapperSubscriptionNotExistsException ex) {
            throw new ScrapperControllerEntityNotFoundException(ex.getMessage(), ex);
        }
    }

    @PostMapping("links/tags")
    public final ResponseEntity<?> addTagsForSubscription(
        @RequestHeader("Tg-Chat-Id") final Long id,
        @RequestBody @Valid final AddLinkTagsRequest request) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        ListLinkTagsResponse response = scrapperService.addTagsForSubscription(id, request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @DeleteMapping("links/tags")
    public final ResponseEntity<?> deleteTagsForSubscription(
        @RequestHeader("Tg-Chat-Id") final Long id,
        @RequestBody @Valid final RemoveLinkTagRequest request) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }try {
            LinkTagResponse response = scrapperService.deleteTagsForSubscription(id, request);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (ScrapperTagNotExistsException e){
            throw new ScrapperControllerEntityNotFoundException("Tag "+request.tag()+" for link "+request.link()+" not exists", e);
        }
    }

    @DeleteMapping("tags/{tag}/links")
    public final ResponseEntity<?> deleteSubscriptionsWithTag(
        @RequestHeader("Tg-Chat-Id") final Long id,
        @PathVariable final String tag) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        ListTagLinksResponse response = scrapperService.deleteSubscriptionsForTag(id, tag);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @DeleteMapping("/tags")
    public final ResponseEntity<?> deleteUserTag(@RequestHeader("Tg-Chat-Id") final Long id,
                                                 @RequestBody @Valid final RemoveTagRequest request) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        try {
            TagResponse response = scrapperService.deleteTag(id, request);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (ScrapperTagNotExistsException e){
            throw new ScrapperControllerEntityNotFoundException("Tag "+request.tag()+" not found", e);
        }
    }

    @GetMapping("/tags")
    public final ResponseEntity<?> getUserTags(@RequestHeader("Tg-Chat-Id") final Long id) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        ListTagsResponse response = scrapperService.getTags(id);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @GetMapping("tags/{tag}/links")
    public final ResponseEntity<?> getSubscriptionsWithTag(
        @RequestHeader("Tg-Chat-Id") final Long id,
        @PathVariable final String tag) {
        if (id <= 0) {
            throw new ScrapperInvalidIdException("Id must be a positive integer");
        }
        ListTagLinksResponse response = scrapperService.deleteSubscriptionsForTag(id, tag);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }
}
