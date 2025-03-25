package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.UserService;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@AllArgsConstructor
public class SqlUserService implements UserService {

    private final UserSqlRepository userRepo;

    @Override
    @Transactional
    public void addUser(User user) {
        try {
            Optional<SqlUser> existingUser = userRepo.findUserByChatId(user.chatId());
            if (existingUser.isPresent()) {
                throw new ScrapperUserAlreadyExistsException("User " + user.chatId() + " already exists");
            }
            SqlUser userForDb = new SqlUser(user.chatId());
            userRepo.addUser(userForDb);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding user with SQL", e);
        }
    }

    @Override
    public void deleteUser(User user) {

    }
}
