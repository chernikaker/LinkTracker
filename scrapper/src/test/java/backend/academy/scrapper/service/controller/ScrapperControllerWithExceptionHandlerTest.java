package backend.academy.scrapper.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.AddLinkTagsRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkTagResponse;
import backend.academy.dto.ListLinkTagsResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.ListTagLinksResponse;
import backend.academy.dto.ListTagsResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.dto.RemoveLinkTagRequest;
import backend.academy.dto.RemoveTagRequest;
import backend.academy.dto.TagResponse;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import backend.academy.scrapper.service.ApplicationExceptionHandler;
import backend.academy.scrapper.service.ScrapperController;
import backend.academy.scrapper.service.ScrapperService;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ScrapperController.class, ApplicationExceptionHandler.class})
public class ScrapperControllerWithExceptionHandlerTest {

    private static final String ADD_TAG_REQUEST =
        """
        {
            "link": "url",
            "tags": [
                "tag"
            ]
        }
        """;

    private static final String REMOVE_SUB_TAG_REQUEST= """
        {
        "link": "url",
        "tag": "tag"
        }
        """;

    private static final String REMOVE_TAG_REQUEST =
                      """
                      {
                      "tag" : "tag"
                      }
                    """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScrapperService scrapperService;

    @Test
    @SneakyThrows
    public void registerChatTest_validId() {
        long invalidChatId = 1L;

        mockMvc.perform(post("/tg-chat/{id}", invalidChatId)).andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void registerChatTest_invalidId() {
        long invalidChatId = -1L;

        mockMvc.perform(post("/tg-chat/{id}", invalidChatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_validId() {

        long chatId = 1L;

        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_invalidId() {
        long invalidChatId = -1L;

        mockMvc.perform(delete("/tg-chat/{id}", invalidChatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void deleteChatTest_serviceException() {
        long chatId = 1L;
        doThrow(new ScrapperUserNotExistsException("message"))
                .when(scrapperService)
                .deleteUser(chatId);

        mockMvc.perform(delete("/tg-chat/{id}", chatId))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("message"));
    }

    @Test
    @SneakyThrows
    public void getLinksTest_successful() {
        long chatId = 1L;
        ListLinksResponse response = new ListLinksResponse(List.of(), 0);
        when(scrapperService.getUserLinks(chatId)).thenReturn(response);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(0));
    }

    @Test
    @SneakyThrows
    public void getLinksTest_invalidId() {
        long invalidChatId = -1L;

        mockMvc.perform(get("/links").header("Tg-Chat-Id", invalidChatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void getLinksTest_serviceException_userNotFound() {
        long chatId = 1L;
        doThrow(new ScrapperUserNotExistsException("message"))
                .when(scrapperService)
                .getUserLinks(chatId);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperUserNotExistsException"))
                .andExpect(jsonPath("$.exceptionMessage").value("message"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscriptionTest_success() {
        long chatId = 1L;
        AddLinkRequest request = new AddLinkRequest("https://example.com", List.of(), List.of());
        LinkResponse response = new LinkResponse(1L, "https://example.com", List.of(), List.of());
        when(scrapperService.addSubscription(chatId, request)).thenReturn(response);

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://example.com",
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.filters").isEmpty());
    }

    @Test
    @SneakyThrows
    public void addLinkSubscription_invalidIdException() {
        long invalidChatId = -1L;

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", invalidChatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "https://example.com",
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscription_invalidInputRequest_nullLink() {
        long chatId = 1L;

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscription_invalidInputRequest_tagsNull() {
        long chatId = 1L;

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "",
                                "filters": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscription_invalidInputRequest_filtersNull() {
        long chatId = 1L;

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "",
                                "tags": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscription_invalidInputRequest_linkEmpty() {
        long chatId = 1L;

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "",
                                "tags": [],
                                "filters": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void addLinkSubscription_invalidInputRequest_unreadableHttp() {
        long chatId = 1L;

        mockMvc.perform(
                        post("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "",
                                "tags": "invalid",
                                "filters": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("HttpMessageNotReadableException"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_success() {
        long chatId = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest("url");
        LinkResponse response = new LinkResponse(1L, "url", List.of(), List.of());
        when(scrapperService.deleteSubscription(chatId, request)).thenReturn(response);

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "url"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value("url"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_invalidIdException() {
        long invalidChatId = -1L;

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", invalidChatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "url"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_invalidLinkException() {
        long chatId = 1L;

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": ""
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_linkNotExistsException() {
        long chatId = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest("url");
        when(scrapperService.deleteSubscription(chatId, request))
                .thenThrow(new ScrapperLinkNotExistsException("Link not found"));

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "url"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Link not found"));
    }

    @Test
    @SneakyThrows
    public void deleteLinkSubscription_subscriptionNotExistsException() {
        long chatId = 1L;
        RemoveLinkRequest request = new RemoveLinkRequest("url");
        when(scrapperService.deleteSubscription(chatId, request))
                .thenThrow(new ScrapperSubscriptionNotExistsException("Subscription not found"));

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", chatId)
                                .contentType("application/json")
                                .content(
                                        """
                            {
                                "link": "url"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Subscription not found"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_success() {
        long chatId = 1L;
        AddLinkTagsRequest request = new AddLinkTagsRequest("url", List.of("tag"));
        ListLinkTagsResponse response = new ListLinkTagsResponse("url",
            new ListTagsResponse(List.of(new TagResponse(1L, "tag")), 1L));
        when(scrapperService.addTagsForSubscription(chatId, request)).thenReturn(response);

        mockMvc.perform(
                post("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(ADD_TAG_REQUEST))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("url"))
            .andExpect(jsonPath("$.tags.size").value(1L))
             .andExpect(jsonPath("$.tags.tags[0].id").value(1L))
            .andExpect(jsonPath("$.tags.tags.[0].value").value("tag"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_invalidRequestSchema() {
        long chatId = 1L;
        AddLinkTagsRequest request = new AddLinkTagsRequest("url", List.of("tag"));
        ListLinkTagsResponse response = new ListLinkTagsResponse("url",
            new ListTagsResponse(List.of(new TagResponse(1L, "tag")), 1L));
        when(scrapperService.addTagsForSubscription(chatId, request)).thenReturn(response);

        mockMvc.perform(
                post("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(
                        """
                    """
                    ))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("HttpMessageNotReadableException"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_InvalidId() {
        long chatId = -1L;

        mockMvc.perform(
                post("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(ADD_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
            .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_ScrapperBadRequestException() {
        long chatId = 1L;
        when(scrapperService.addTagsForSubscription(eq(chatId), any())).thenThrow(new ScrapperUserNotExistsException("error"));

        mockMvc.perform(
                post("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(ADD_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperUserNotExistsException"))
            .andExpect(jsonPath("$.exceptionMessage").value("error"));
    }

    @Test
    @SneakyThrows
    public void addTagsForSubscription_ScrapperInternalException() {
        long chatId = 1L;
        when(scrapperService.addTagsForSubscription(eq(chatId), any())).thenThrow(new ScrapperSqlException("error"));

        mockMvc.perform(
                post("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(ADD_TAG_REQUEST))
            .andExpect(status().is5xxServerError())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperSqlException"))
            .andExpect(jsonPath("$.exceptionMessage").value("error"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_success() {
        long chatId = 1L;
        RemoveLinkTagRequest request = new RemoveLinkTagRequest("url", "tag");
        LinkTagResponse response = new LinkTagResponse(new TagResponse(1L, "tag"),"url");
        when(scrapperService.deleteTagForSubscription(chatId, request)).thenReturn(response);

        mockMvc.perform(
                delete("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("url"))
            .andExpect(jsonPath("$.tag.id").value(1L))
            .andExpect(jsonPath("$.tag.value").value("tag"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_InvalidId() {
        long chatId = -1L;

        mockMvc.perform(
                delete("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
            .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_ScrapperBadRequestException() {
        long chatId = 1L;
        when(scrapperService.deleteTagForSubscription(eq(chatId), any()))
            .thenThrow(new ScrapperSubscriptionNotExistsException("error"));

        mockMvc.perform(
                delete("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperSubscriptionNotExistsException"))
            .andExpect(jsonPath("$.exceptionMessage").value("error"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_ScrapperEntityNotFoundException() {
        long chatId = 1L;
        when(scrapperService.deleteTagForSubscription(eq(chatId), any()))
            .thenThrow(new ScrapperTagNotExistsException("error"));

        mockMvc.perform(
                delete("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    @Test
    @SneakyThrows
    public void deleteTagForSubscription_InternalException() {
        long chatId = 1L;
        when(scrapperService.deleteTagForSubscription(eq(chatId), any()))
            .thenThrow(new ScrapperSqlException("error"));

        mockMvc.perform(
                delete("/links/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_SUB_TAG_REQUEST))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_Success(){
        long chatId = 1L;
        String tag = "tag";
        LinkResponse link = new LinkResponse(1L, "url", List.of("tag"), List.of("filter"));
        ListTagLinksResponse response = new ListTagLinksResponse(tag,
            new ListLinksResponse(List.of(link), 1));
        when(scrapperService.deleteSubscriptionsForTag(chatId, tag)).thenReturn(response);

        mockMvc.perform(
                delete("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tag").value("tag"))
            .andExpect(jsonPath("$.links.size").value(1L))
            .andExpect(jsonPath("$.links.links[0].id").value(1L))
            .andExpect(jsonPath("$.links.links[0].url").value("url"));
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_InvalidIdException(){
        long chatId = -1L;

        mockMvc.perform(
                delete("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
            .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_BadRequestException(){
        long chatId = 1L;
        when(scrapperService.deleteSubscriptionsForTag(chatId, "tag"))
            .thenThrow(new ScrapperTagNotExistsException("error"));

        mockMvc.perform(
                delete("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperTagNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void deleteSubscriptionsWithTag_ScrapperInternalException(){
        long chatId = 1L;
        when(scrapperService.deleteSubscriptionsForTag(chatId, "tag"))
            .thenThrow(new ScrapperOrmException("error"));

        mockMvc.perform(
                delete("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_Success(){
        long chatId = 1L;
        RemoveTagRequest request = new RemoveTagRequest("tag");
        TagResponse response = new TagResponse(1L, "tag");
        when(scrapperService.deleteTag(chatId, request)).thenReturn(response);

        mockMvc.perform(
                delete("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.value").value("tag"))
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_InvalidIdException(){
        long chatId = -1L;

        mockMvc.perform(
                delete("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
            .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_BadRequestException(){
        long chatId = 1L;
        when(scrapperService.deleteTag(eq(chatId), any())).thenThrow(
            new ScrapperUserNotExistsException("error")
        );

        mockMvc.perform(
                delete("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperUserNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_EntityNotFoundException(){
        long chatId = 1L;
        when(scrapperService.deleteTag(eq(chatId), any())).thenThrow(
            new ScrapperTagNotExistsException("error")
        );

        mockMvc.perform(
                delete("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_TAG_REQUEST))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperControllerEntityNotFoundException"));
    }

    @Test
    @SneakyThrows
    public void deleteUserTag_ScrapperInternalException(){
        long chatId = 1L;
        when(scrapperService.deleteTag(eq(chatId), any())).thenThrow(
            new ScrapperOrmException("error")
        );

        mockMvc.perform(
                delete("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(REMOVE_TAG_REQUEST))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @SneakyThrows
    public void getUserTags_Success(){
        long chatId = 1L;
        ListTagsResponse response = new ListTagsResponse(List.of(new TagResponse(1L, "tag")), 1L);
        when(scrapperService.getTags(eq(chatId))).thenReturn(response);

        mockMvc.perform(
                get("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(1L))
            .andExpect(jsonPath("$.tags[0].id").value(1L))
            .andExpect(jsonPath("$.tags[0].value").value("tag"));
    }

    @Test
    @SneakyThrows
    public void getUserTags_SuccessNoTags(){
        long chatId = 1L;
        ListTagsResponse response = new ListTagsResponse(List.of(), 0L);
        when(scrapperService.getTags(eq(chatId))).thenReturn(response);

        mockMvc.perform(
                get("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(0L));
    }

    @Test
    @SneakyThrows
    public void getUserTags_InvalidIdException(){
        long chatId = -1L;

        mockMvc.perform(
                get("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
            .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void getUserTags_BadRequestException(){
        long chatId = 1L;
        when(scrapperService.getTags(chatId)).thenThrow(new ScrapperUserNotExistsException("error"));

        mockMvc.perform(
                get("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperUserNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void getUserTags_ScrapperInternalException(){
        long chatId = 1L;
        when(scrapperService.getTags(chatId)).thenThrow(new ScrapperOrmException("error"));

        mockMvc.perform(
                get("/tags")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_Success(){
        long chatId = 1L;
        String tag = "tag";
        LinkResponse link = new LinkResponse(1L, "url", List.of("tag"), List.of("filter"));
        ListTagLinksResponse response = new ListTagLinksResponse(tag,
            new ListLinksResponse(List.of(link), 1));
        when(scrapperService.deleteSubscriptionsForTag(chatId, tag)).thenReturn(response);

        mockMvc.perform(
                get("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tag").value("tag"))
            .andExpect(jsonPath("$.links.size").value(1L))
            .andExpect(jsonPath("$.links.links[0].id").value(1L))
            .andExpect(jsonPath("$.links.links[0].url").value("url"));
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_SuccessNoSubs(){
        long chatId = 1L;
        String tag = "tag";
        ListTagLinksResponse response = new ListTagLinksResponse(tag,
            new ListLinksResponse(List.of(), 0));
        when(scrapperService.deleteSubscriptionsForTag(chatId, tag)).thenReturn(response);

        mockMvc.perform(
                get("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tag").value("tag"))
            .andExpect(jsonPath("$.links.size").value(0L));
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_InvalidIdException(){
        long chatId = -1L;

        mockMvc.perform(
                get("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperInvalidIdException"))
            .andExpect(jsonPath("$.exceptionMessage").value("Id must be a positive integer"));
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_BadRequestException(){
        long chatId = 1L;
        when(scrapperService.deleteSubscriptionsForTag(chatId, "tag"))
            .thenThrow(new ScrapperTagNotExistsException("error"));

        mockMvc.perform(
                get("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("ScrapperTagNotExistsException"));
    }

    @Test
    @SneakyThrows
    public void getSubscriptionsWithTag_ScrapperInternalException(){
        long chatId = 1L;
        when(scrapperService.deleteSubscriptionsForTag(chatId, "tag"))
            .thenThrow(new ScrapperOrmException("error"));

        mockMvc.perform(
                get("/tags/tag/links")
                    .header("Tg-Chat-Id", chatId)
                    .contentType("application/json")
                    .content(""))
            .andExpect(status().is5xxServerError());
    }


}
