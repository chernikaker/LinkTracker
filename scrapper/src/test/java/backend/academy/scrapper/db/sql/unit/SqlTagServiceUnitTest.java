package backend.academy.scrapper.db.sql.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.entity.SqlTag;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.TagSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.db.sql.service.SqlTagService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlTagServiceUnitTest {

    private static final long chatId = 123L;
    private static final String tagText = "java";
    private static final long tagId = 10L;
    private static final String url = "https://github.com/1";

    private static final long userId = 1L;
    private static final long linkId = 10L;
    private static final long subId = 100L;

    private static User user;
    private static Tag tag;
    private static Link link;

    private static SqlUser sqlUser;
    private static SqlLink sqlLink;
    private static SqlSubscription sqlSub;
    private static SqlTag sqlTag;

    @Mock
    private UserSqlRepository userRepo;

    @Mock
    private LinkSqlRepository linkRepo;

    @Mock
    private SubscriptionSqlRepository subscrRepo;

    @Mock
    private TagSqlRepository tagRepo;

    @InjectMocks
    private SqlTagService sqlTagService;

    @BeforeAll
    public static void setUp() {
        user = new User(chatId);
        tag = new Tag(tagText);
        link = new Link(url, LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

        sqlUser = new SqlUser(userId, chatId);
        sqlLink = new SqlLink(linkId, url, link.lastValidation());
        sqlSub = new SqlSubscription(subId, userId, linkId);
        sqlTag = new SqlTag(tagId, tagText, userId);
    }

    @Test
    public void deleteTagForUser_Success() {
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.of(sqlUser));
        when(tagRepo.getTagByTextAndUserId(userId, tagText)).thenReturn(Optional.of(sqlTag));

        assertDoesNotThrow(() -> sqlTagService.deleteTagForUser(user, tag));

        verify(tagRepo).removeTagById(tagId);
    }

    @Test
    public void deleteTagForUser_UserNotFound() {
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlTagService.deleteTagForUser(user, tag))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteTagForUser_TagNotFound() {
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.of(sqlUser));
        when(tagRepo.getTagByTextAndUserId(userId, tagText)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlTagService.deleteTagForUser(user, tag))
                .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscription_Data_Success() {
        when(linkRepo.findLinkByUrl(url)).thenReturn(Optional.of(sqlLink));
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.of(sqlUser));
        when(subscrRepo.getSubscriptionByLinkAndUserId(linkId, userId)).thenReturn(Optional.of(sqlSub));
        when(tagRepo.getTagByTextAndUserId(userId, tagText)).thenReturn(Optional.of(sqlTag));
        assertDoesNotThrow(() -> sqlTagService.deleteTagForSubscriptionData(user, link, tagText));

        verify(tagRepo).removeTagFromSubscription(tagId, subId);
    }

    @Test
    public void deleteTagForSubscription_Data_NoSuchLinkException() {
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.of(sqlUser));
        when(linkRepo.findLinkByUrl(url)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlTagService.deleteTagForSubscriptionData(user, link, tagText))
                .isInstanceOf(ScrapperLinkNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscription_Data_NoSuchUserException() {
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlTagService.deleteTagForSubscriptionData(user, link, tagText))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscription_NoSuchSubscriptionDataException() {
        when(linkRepo.findLinkByUrl(url)).thenReturn(Optional.of(sqlLink));
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.of(sqlUser));
        when(subscrRepo.getSubscriptionByLinkAndUserId(linkId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlTagService.deleteTagForSubscriptionData(user, link, tagText))
                .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscription_NoSuchTagExceptionData() {
        when(linkRepo.findLinkByUrl(url)).thenReturn(Optional.of(sqlLink));
        when(userRepo.findUserByChatId(chatId)).thenReturn(Optional.of(sqlUser));
        when(subscrRepo.getSubscriptionByLinkAndUserId(linkId, userId)).thenReturn(Optional.of(sqlSub));
        when(tagRepo.getTagByTextAndUserId(userId, tagText)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlTagService.deleteTagForSubscriptionData(user, link, tagText))
                .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscription_Data_DataAccessException() {
        when(userRepo.findUserByChatId(chatId)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> sqlTagService.deleteTagForSubscriptionData(user, link, tagText))
                .isInstanceOf(ScrapperSqlException.class);
    }

    @Test
    public void getTagsBySubscriptionId_TagsPresent() {
        List<SqlTag> sqlTags = List.of(new SqlTag(1L, tagText, 1L));
        when(tagRepo.getSubscriptionTags(subId)).thenReturn(sqlTags);

        List<Tag> result = assertDoesNotThrow(() -> sqlTagService.getTagsBySubscriptionId(subId));

        assertEquals(1, result.size());
        assertEquals(tagText, result.getFirst().value());
    }

    @Test
    public void getTagsBySubscriptionId_TagsEmpty() {
        List<SqlTag> sqlTags = List.of();
        when(tagRepo.getSubscriptionTags(subId)).thenReturn(sqlTags);

        List<Tag> result = assertDoesNotThrow(() -> sqlTagService.getTagsBySubscriptionId(subId));

        assertTrue(result.isEmpty());
    }

    @Test
    public void getTagsBySubscriptionId_DataAccessException() {
        when(tagRepo.getSubscriptionTags(subId)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> sqlTagService.getTagsBySubscriptionId(subId)).isInstanceOf(ScrapperSqlException.class);
    }
}
