package backend.academy.scrapper.service.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import backend.academy.scrapper.service.ApplicationExceptionHandler;
import backend.academy.scrapper.service.ScrapperController;
import backend.academy.scrapper.service.ScrapperService;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ScrapperController.class, ApplicationExceptionHandler.class})
public class ScrapperControllerWithExceptionHandlerTest {

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
        long invalidChatId = -1L;

        mockMvc.perform(
                        delete("/links")
                                .header("Tg-Chat-Id", invalidChatId)
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
    public void deleteLinkSubscription_scrapperNotExistsException() {
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
}
