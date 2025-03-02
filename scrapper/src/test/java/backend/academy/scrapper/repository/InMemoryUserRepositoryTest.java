package backend.academy.scrapper.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryUserRepositoryTest {

    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
    }

    @Test
    public void registerNewUser() {
        User user = new User(1L);

        long userId = userRepository.registerUser(user);

        assertEquals(1L, userId);
    }

    @Test
    public void registerUser_UserAlreadyExists() {
        User user = new User(1L);
        userRepository.registerUser(user);

        assertThatThrownBy(() -> userRepository.registerUser(user))
                .isInstanceOf(ScrapperUserAlreadyExistsException.class);
    }

    @Test
    public void getUserById_UserExists() {
        User user = new User(1L);
        userRepository.registerUser(user);

        User foundUser = userRepository.getUserById(1L);

        assertEquals(user, foundUser);
    }

    @Test
    public void getUserById_UserDoesNotExist() {
        long nonExistentUserId = 999L;

        assertThatThrownBy(() -> userRepository.getUserById(nonExistentUserId))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteUserById_UserExists() {
        User user = new User(1L);
        userRepository.registerUser(user);

        assertDoesNotThrow(() -> userRepository.deleteUserById(1L));
        assertThatThrownBy(() -> userRepository.getUserById(1L)).isInstanceOf(ScrapperUserNotExistsException.class);
    }

    @Test
    public void deleteUserById_UserDoesNotExist() {
        long nonExistentUserId = 999L;

        assertThatThrownBy(() -> userRepository.getUserById(nonExistentUserId))
                .isInstanceOf(ScrapperUserNotExistsException.class);
    }
}
