package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.UserService;
import backend.academy.scrapper.db.sql.entity.SqlSubscription;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class SqlUserService implements UserService {

    private final UserSqlRepository userRepo;
    private final SubscriptionSqlRepository subscriptionRepo;
    private final LinkSqlRepository linkRepo;

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
    @Transactional
    public void deleteUser(User user) {
        try {
            SqlUser userDb = userRepo.findUserByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserAlreadyExistsException("User " + user.chatId() + " already exists"));
            subscriptionRepo.getSubscriptionsByUserId(userDb.id())
                .stream()
                .map(SqlSubscription::linkId)
                .filter((id) -> subscriptionRepo.getSubscriptionsByLink(id).isEmpty())
                .forEach(linkRepo::deleteLinkById);
            userRepo.deleteUserById(userDb.id());
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while deleting user with SQL", e);
        }
    }
}
