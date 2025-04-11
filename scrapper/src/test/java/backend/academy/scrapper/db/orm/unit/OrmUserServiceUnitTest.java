package backend.academy.scrapper.db.orm.unit;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.db.orm.service.OrmUserService;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrmUserServiceUnitTest {

    private static final long CHAT_ID = 12345L;
    private static final String URL = "https://github.com/";

    @Mock
    private OrmUserRepository userRepo;

    @Mock
    private OrmLinkRepository linkRepo;

    @InjectMocks
    private OrmUserService userService;

    private User testUser;
    private OrmUser existingOrmUser;
    private OrmUser newOrmUser;
    private OrmLink testLink;

    @BeforeEach
    void setUp() {
        testUser = new User(CHAT_ID);
        newOrmUser = new OrmUser(1L, CHAT_ID, null, null, null);

        OrmSubscription testSubscription = new OrmSubscription();

        testLink = new OrmLink(1L, URL, LocalDateTime.now(ZoneId.systemDefault()), List.of(testSubscription));
        existingOrmUser = new OrmUser(2L, CHAT_ID, List.of(testSubscription), null, null);

        testSubscription.id(1L);
        testSubscription.link(testLink);
        testSubscription.user(existingOrmUser);
    }

    @Test
    public void addUser_Success() {
        when(userRepo.findByChatId(CHAT_ID)).thenReturn(Optional.empty());
        when(userRepo.save(any(OrmUser.class))).thenReturn(newOrmUser);

        assertDoesNotThrow(() -> userService.addUser(testUser));

        verify(userRepo).findByChatId(CHAT_ID);
        verify(userRepo).save(any(OrmUser.class));
    }

    @Test
    public void addUser_UserAlreadyExists() {
        when(userRepo.findByChatId(CHAT_ID)).thenReturn(Optional.of(existingOrmUser));

        assertThatThrownBy(() -> userService.addUser(testUser))
            .isInstanceOf(ScrapperUserAlreadyExistsException.class);

        verify(userRepo, never()).save(any());
    }

    @Test
    public void addUser_DatabaseError() {
        when(userRepo.findByChatId(CHAT_ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> userService.addUser(testUser))
            .isInstanceOf(ScrapperOrmException.class);
    }

    @Test
    public void deleteUser_successWithOrphanedLinks() {
        when(userRepo.findByChatId(CHAT_ID)).thenReturn(Optional.of(existingOrmUser));
        doAnswer(i -> testLink.subscriptions(List.of())).when(userRepo).delete(any());

        assertDoesNotThrow(() -> userService.deleteUser(testUser));

        verify(userRepo).delete(existingOrmUser);
        verify(userRepo).flush();
        verify(linkRepo).delete(testLink);
        verify(linkRepo).flush();
    }

    @Test
    public void deleteUser_successNoOrphanedLinks() {
        when(userRepo.findByChatId(CHAT_ID)).thenReturn(Optional.of(existingOrmUser));

        assertDoesNotThrow(() -> userService.deleteUser(testUser));

        verify(linkRepo, never()).delete(any());
        verify(linkRepo).flush();
    }


    @Test
    void deleteUser_UserNotFound() {
        when(userRepo.findByChatId(CHAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(new User(CHAT_ID)))
            .isInstanceOf(ScrapperUserNotExistsException.class)
            .hasMessageContaining("does not exist");

        verify(userRepo, never()).delete(any());
    }

    @Test
    void deleteUser_DatabaseError() {
        when(userRepo.findByChatId(CHAT_ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> userService.deleteUser(testUser))
            .isInstanceOf(ScrapperOrmException.class);
    }
}
