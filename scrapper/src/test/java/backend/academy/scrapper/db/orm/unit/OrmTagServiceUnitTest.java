package backend.academy.scrapper.db.orm.unit;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmTag;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.repository.OrmSubscriptionRepository;
import backend.academy.scrapper.db.orm.repository.OrmTagRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.db.orm.service.OrmTagService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperSubscriptionNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperTagNotExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrmTagServiceUnitTest {

    private static final long ID = 1L;
    private static final String TAG_TEXT = "test_tag";
    private static final String URL = "https://example.com";

    private static final User USER = new User(ID);
    private static final Link LINK = new Link(URL, LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
    private static final Tag TAG = new Tag(TAG_TEXT);

    private static OrmUser ORM_USER = new OrmUser();
    private static OrmSubscription ORM_SUBSCRIPTION = new OrmSubscription();
    private static OrmTag ORM_TAG = new OrmTag();

    @Mock
    private OrmUserRepository userRepo;

    @Mock
    private OrmSubscriptionRepository subscrRepo;

    @Mock
    private OrmTagRepository tagRepo;

    @InjectMocks
    private OrmTagService tagService;

    @BeforeAll
    public static void setUp(){
        ORM_USER = new OrmUser(ID, ID, List.of(ORM_SUBSCRIPTION), List.of(), List.of());

        ORM_SUBSCRIPTION.id(ID);
        ORM_SUBSCRIPTION.user(ORM_USER);

        OrmLink l = new OrmLink();
        l.url(URL);
        ORM_SUBSCRIPTION = new OrmSubscription(l, ORM_USER);

        ORM_TAG = new OrmTag(ID, TAG_TEXT, ORM_USER, List.of(ORM_SUBSCRIPTION));
    }

    @Test
    public void deleteTagForUser_Success() {
        ORM_USER.tags(new ArrayList<>(List.of(ORM_TAG)));
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        assertDoesNotThrow(()->tagService.deleteTagForUser(USER, TAG));

        assertThat(ORM_USER.tags()).doesNotContain(ORM_TAG);
        verify(tagRepo).delete(ORM_TAG);
        verify(tagRepo).flush();
    }

    @Test
    public void deleteTagForUser_TagNotExists() {
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        assertThatThrownBy(() -> tagService.deleteTagForUser(USER, TAG))
            .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForUser_DataAccessException() {
        when(userRepo.findByChatId(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> tagService.deleteTagForUser(USER, TAG))
            .isInstanceOf(ScrapperOrmException.class);
    }


    @Test
    public void deleteTagForSubscriptionData_success() {
        ORM_USER.subscriptions(new ArrayList<>(List.of(ORM_SUBSCRIPTION)));
        ORM_SUBSCRIPTION.tags(new ArrayList<>(List.of(ORM_TAG)));
        ORM_TAG.subscriptions(new ArrayList<>(List.of(ORM_SUBSCRIPTION)));
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        assertDoesNotThrow(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG_TEXT));

        assertThat(ORM_SUBSCRIPTION.tags()).doesNotContain(ORM_TAG);
        assertThat(ORM_TAG.subscriptions()).doesNotContain(ORM_SUBSCRIPTION);
        verify(subscrRepo).save(ORM_SUBSCRIPTION);
        verify(subscrRepo).flush();
    }

    @Test
    public void deleteTagForSubscriptionData_userNotFound() {
        when(userRepo.findByChatId(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG_TEXT))
            .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscriptionData_subscriptionNotFound() {
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));
        ORM_USER.subscriptions(List.of());

        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG_TEXT))
            .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscriptionData_TagNotFound() {
        ORM_USER.subscriptions(List.of(ORM_SUBSCRIPTION));
        ORM_SUBSCRIPTION.tags(List.of());
        when(userRepo.findByChatId(ID)).thenReturn(Optional.of(ORM_USER));

        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG_TEXT))
            .isInstanceOf(ScrapperTagNotExistsException.class);
    }

    @Test
    public void deleteTagForSubscriptionData_DataAccessException() {
        when(userRepo.findByChatId(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> tagService.deleteTagForSubscriptionData(USER, LINK, TAG_TEXT))
            .isInstanceOf(ScrapperOrmException.class);
    }

    @Test
    public void getTagsBySubscriptionId_successTagsExist() {
        ORM_SUBSCRIPTION.tags(List.of(ORM_TAG));
        when(subscrRepo.findById(ID)).thenReturn(Optional.of(ORM_SUBSCRIPTION));


        List<Tag> result = assertDoesNotThrow(() -> tagService.getTagsBySubscriptionId(ID));

        assertEquals(1, result.size());
        assertEquals(TAG_TEXT, result.getFirst().value());
    }

    @Test
    public void getTagsBySubscriptionId_successNoTags() {
        ORM_SUBSCRIPTION.tags(List.of());
        when(subscrRepo.findById(ID)).thenReturn(Optional.of(ORM_SUBSCRIPTION));

        List<Tag> result = assertDoesNotThrow(() -> tagService.getTagsBySubscriptionId(ID));

        assertTrue(result.isEmpty());
    }

    @Test
    public void getTagsBySubscriptionId_subscriptionNotFound() {
        when(subscrRepo.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagsBySubscriptionId(ID))
            .isInstanceOf(ScrapperSubscriptionNotExistsException.class);
    }

    @Test
    public void getTagsBySubscriptionId_DataAccessException() {
        when(subscrRepo.findById(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> tagService.getTagsBySubscriptionId(ID))
            .isInstanceOf(ScrapperOrmException.class);
    }
}
