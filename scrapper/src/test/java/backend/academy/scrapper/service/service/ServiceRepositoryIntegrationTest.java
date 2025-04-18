package backend.academy.scrapper.service.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.db.config.SqlConfig;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.service.ScrapperUnavailableLinkException;
import backend.academy.scrapper.service.ScrapperService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.github.dockerjava.core.dockerfile.DockerfileStatement;

@SpringBootTest
@Import(SqlConfig.class)
@Transactional
public class ServiceRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScrapperService scrapperService;

    @MockitoBean
    private GithubClientService githubClientService;

    @MockitoBean
    private StackoverflowClientService stackoverflowClientService;

    private User user;
    private Link link;
    private Tag tag;
    private Filter filter;

    @BeforeEach
    public void setUp() {
        user = new User(1L);
        link = new Link("https://github.com/mock", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        tag = new Tag("tag");
        filter = new Filter("key", "value");
    }

    @Test
    public void testRegisterUser_Success() {
        assertDoesNotThrow(() -> scrapperService.registerUser(1L));

        assertEquals(1, getUserAmountByChat(1L));
    }

    @Test
    public void testDeleteUser_Success() {
        addUserWithChatId(1L);

        assertDoesNotThrow(() -> scrapperService.deleteUser(1L));

        assertEquals(0, getUserAmountByChat(1L));
    }

    @Test
    public void testGetUserLinks_success() {
        fillAllData(true);

        ListLinksResponse response = assertDoesNotThrow(() -> scrapperService.getUserLinks(1L));

        assertEquals(1, response.size());
        LinkResponse linkRes = response.links().getFirst();
        assertEquals(link.url(), linkRes.url());
        assertEquals(1, linkRes.tags().size());
        assertEquals(tag.value(), linkRes.tags().getFirst());
        assertEquals(1, linkRes.filters().size());
        assertEquals(filter.key() + ":" + filter.value(), linkRes.filters().getFirst());
    }

    @Test
    public void testGetUserLinks_noLinks() {
        addUserWithChatId(1L);

        ListLinksResponse response = assertDoesNotThrow(() -> scrapperService.getUserLinks(1L));

        assertEquals(0, response.size());
    }

    @Test
    public void testAddSubscription_Success() {
        addUserWithChatId(1L);
        when(githubClientService.isLinkAvailable(any())).thenReturn(true);
        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        LinkResponse response = assertDoesNotThrow(() -> scrapperService.addSubscription(1L, request));

        assertEquals("https://github.com/mock", response.url());
        assertTrue(response.tags().isEmpty());
        assertTrue(response.filters().isEmpty());

        assertEquals(1, getSubAmountByChatIdAndLinkUrl(1L, "https://github.com/mock"));
    }

    @Test
    public void testAddSubscription_LinkUnavailable() {
        addUserWithChatId(1L);
        when(githubClientService.isLinkAvailable(any(Link.class))).thenReturn(false);
        AddLinkRequest request = new AddLinkRequest("https://github.com/mock", List.of(), List.of());

        assertThatThrownBy(() -> scrapperService.addSubscription(1L, request))
                .isInstanceOf(ScrapperUnavailableLinkException.class);
    }

    @Test
    public void testDeleteSubscription_Success() {
        fillAllData(false);

        RemoveLinkRequest request = new RemoveLinkRequest(link.url());
        LinkResponse response = assertDoesNotThrow(() -> scrapperService.deleteSubscription(user.chatId(), request));

        assertEquals(link.url(), response.url());
        assertEquals(0, getSubAmountByChatIdAndLinkUrl(user.chatId(), link.url()));
    }

    @Test
    public void addTagsForSubscription_Success(){
        fillAllData(false);
        AddLinkTagsRequest request = new AddLinkTagsRequest(link.url(), List.of(tag.value()));

        ListLinkTagsResponse response = assertDoesNotThrow(() -> scrapperService.addTagsForSubscription(user.chatId(), request));
        assertEquals(1, response.tags().size());
        assertEquals(tag.value(), response.tags().tags().getFirst().value());
        assertTrue(response.tags().tags().getFirst().id()>=1);
        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription_tag"));
    }

    @Test
    public void addTagsForSubscription_SuccessReturnsTagsThatExist(){
        fillAllData(true);
        AddLinkTagsRequest request = new AddLinkTagsRequest(link.url(), List.of(tag.value()));

        ListLinkTagsResponse response = assertDoesNotThrow(() -> scrapperService.addTagsForSubscription(user.chatId(), request));
        assertEquals(1, response.tags().size());
        assertEquals(tag.value(), response.tags().tags().getFirst().value());
        assertEquals(1, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription_tag"));
    }

    @Test
    public void deleteTagForSubscription_Success(){
        fillAllData(true);
        RemoveLinkTagRequest request = new RemoveLinkTagRequest(link.url(), tag.value());

        LinkTagResponse response = assertDoesNotThrow(() -> scrapperService.deleteTagForSubscription(user.chatId(), request));
        assertEquals(tag.value(), response.tag().value());
        assertTrue(response.tag().id()>=1);
        assertEquals(1, findAllAmount("tag"));
        assertEquals(0, findAllAmount("subscription_tag"));
    }

    @Test
    public void deleteSubscriptionsForTag_Success(){
        fillAllData(true);

        ListTagLinksResponse response = assertDoesNotThrow(() -> scrapperService.deleteSubscriptionsForTag(user.chatId(), tag.value()));
        assertEquals(tag.value(), response.tag());
        assertEquals(1, response.links().size());
        assertTrue(response.links().links().getFirst().id()>=1);
        assertEquals(0, findAllAmount("subscription"));
        assertEquals(0, findAllAmount("subscription_tag"));
    }

    @Test
    public void deleteTag_Success(){
        fillAllData(true);

        RemoveTagRequest request = new RemoveTagRequest(tag.value());
        TagResponse response = assertDoesNotThrow(() -> scrapperService.deleteTag(user.chatId(), request));
        assertEquals(tag.value(), response.value());
        assertEquals(0, findAllAmount("subscription_tag"));
        assertEquals(0, findAllAmount("tag"));
        assertEquals(1, findAllAmount("subscription"));
    }

    @Test
    public void getTags_Success(){
        fillAllData(true);

        ListTagsResponse response = assertDoesNotThrow(() -> scrapperService.getTags(user.chatId()));
        assertEquals(1, response.size());
        assertEquals(tag.value(), response.tags().getFirst().value());
    }

    private Long getUserAmountByChat(long chatId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tg_user WHERE chat_id=?", Long.class, chatId);
    }

    private Long getSubAmountByChatIdAndLinkUrl(long chatId, String url) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscription s " + "JOIN tg_user u ON u.id = s.user_id "
                        + "JOIN link l ON l.id = s.link_id WHERE l.url = ? AND u.chat_id = ?",
                Long.class,
                url,
                chatId);
    }

    private Long addUserWithChatId(Long chatId) {
        return jdbcTemplate.queryForObject("INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id", Long.class, chatId);
    }

    private void fillAllData(boolean withAddInfo) {
        Long userId = addUserWithChatId(user.chatId());
        Long linkId = jdbcTemplate.queryForObject(
                "INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
                Long.class,
                link.url(),
                link.lastValidation());
        Long subscriptionId = jdbcTemplate.queryForObject(
                "INSERT INTO subscription (user_id, link_id) VALUES (?, ?) RETURNING id", Long.class, userId, linkId);
        if (withAddInfo) {
            Long tagId = jdbcTemplate.queryForObject(
                    "INSERT INTO tag (tag_text, user_id) VALUES (?, ?) RETURNING id", Long.class, tag.value(), userId);
            jdbcTemplate.update(
                    "INSERT INTO filter (key, value, subscription_id, user_id) VALUES (?, ?, ?, ?)",
                    filter.key(),
                    filter.value(),
                    subscriptionId,
                    userId);
            jdbcTemplate.update(
                    "INSERT INTO subscription_tag (subscription_id, tag_id) VALUES (?, ?)", subscriptionId, tagId);
        }
    }

    private Long findAllAmount(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }
}
