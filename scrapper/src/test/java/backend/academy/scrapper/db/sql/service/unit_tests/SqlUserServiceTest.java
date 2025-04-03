package backend.academy.scrapper.db.sql.service.unit_tests;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.db.sql.service.SqlUserService;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlUserServiceTest {

    private final static User USER = new User(123L);
    private final static SqlUser SQL_USER = new SqlUser(1L, USER.chatId());

    @Mock
    private UserSqlRepository userRepo;

    @Mock
    private SubscriptionSqlRepository subscriptionRepo;

    @Mock
    private LinkSqlRepository linkRepo;

    @InjectMocks
    private SqlUserService sqlUserService;

    @Test
    public void addUser_AddNewUser() {
        when(userRepo.findUserByChatId(USER.chatId())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> sqlUserService.addUser(USER));

        verify(userRepo).findUserByChatId(USER.chatId());
        verify(userRepo).addUser(any(SqlUser.class));
    }

    @Test
    public void addUser_UserExists() {
        when(userRepo.findUserByChatId(USER.chatId())).thenReturn(Optional.of(SQL_USER));

        assertThatThrownBy(() -> sqlUserService.addUser(USER))
            .isInstanceOf(ScrapperUserAlreadyExistsException.class);
        verify(userRepo, never()).addUser(any());
    }

    @Test
    public void addUser_DataAccessError() {
        when(userRepo.findUserByChatId(USER.chatId())).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> sqlUserService.addUser(USER))
            .isInstanceOf(ScrapperSqlException.class);
    }


    @Test
    public void deleteUser_DeleteUserAndOrphanLink() {
        SqlSubscription subscription = new SqlSubscription(1L, 1L, 1L);
        when(userRepo.findUserByChatId(USER.chatId())).thenReturn(Optional.of(SQL_USER));
        when(subscriptionRepo.getSubscriptionsByUserId(SQL_USER.id()))
            .thenReturn(List.of(subscription));
        when(subscriptionRepo.getSubscriptionsByLink(1L)).thenReturn(List.of());

        assertDoesNotThrow(() -> sqlUserService.deleteUser(USER));

        verify(userRepo).deleteUserById(SQL_USER.id());
        verify(linkRepo).deleteLinkById(1L);
    }

    @Test
    public void deleteUser_DeleteUserAndNoLinks() {
        SqlSubscription subscription = new SqlSubscription(1L, 1L, 1L);
        when(userRepo.findUserByChatId(USER.chatId())).thenReturn(Optional.of(SQL_USER));
        when(subscriptionRepo.getSubscriptionsByUserId(SQL_USER.id()))
            .thenReturn(List.of(subscription));
        when(subscriptionRepo.getSubscriptionsByLink(1L)).thenReturn(List.of(subscription));

        assertDoesNotThrow(() -> sqlUserService.deleteUser(USER));

        verify(userRepo).deleteUserById(SQL_USER.id());
        verify(linkRepo, never()).deleteLinkById(1L);
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepo.findUserByChatId(USER.chatId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sqlUserService.deleteUser(USER))
            .isInstanceOf(ScrapperUserNotExistsException.class);
        verify(userRepo, never()).deleteUserById(USER.chatId());
    }

    @Test
    void deleteUser_DataAccessError() {
        when(userRepo.findUserByChatId(USER.chatId())).thenReturn(Optional.of(SQL_USER));
        when(subscriptionRepo.getSubscriptionsByUserId(SQL_USER.id()))
            .thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> sqlUserService.deleteUser(USER))
            .isInstanceOf(ScrapperSqlException.class);
    }
}
