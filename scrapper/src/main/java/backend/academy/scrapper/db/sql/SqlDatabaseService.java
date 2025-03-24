package backend.academy.scrapper.db.sql;

import backend.academy.scrapper.db.DatabaseService;
import backend.academy.scrapper.db.sql.entity.SqlUser;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.repository.SubscriptionSqlRepository;
import backend.academy.scrapper.db.sql.repository.UserSqlRepository;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@AllArgsConstructor
@Service
public class SqlDatabaseService implements DatabaseService {

    private final UserSqlRepository userRepo;
    private final LinkSqlRepository linkRepo;
    private final SubscriptionSqlRepository subscrRepo;

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
    public void addLink(Link link) {
        try {
            Optional<Link> existingLinkId = linkRepo.findLinkByUrl(link.url());
            if (existingLinkId.isPresent()) {
                return;
            }
            linkRepo.addLink(link);
        } catch (DataAccessException e) {
            throw new ScrapperSqlException("Exception while adding link with SQL", e);
        }
    }


}
