package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class OrmUserService implements UserService {

    private final OrmUserRepository userRepo;
    private final OrmLinkRepository linkRepo;

    @Override
    public void addUser(User user) {
        try {
            Optional<OrmUser> existingUser = userRepo.findByChatId(user.chatId());
            if(existingUser.isPresent()) {
                throw new ScrapperUserAlreadyExistsException("User "+user.chatId()+" already exists");
            }
            OrmUser ormUser = new OrmUser();
            ormUser.chatId(user.chatId());
            userRepo.save(ormUser);
        } catch (DataAccessException e){
            throw new ScrapperOrmException("Error while adding user with ORM", e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(User user) {
        try {
            OrmUser existingUser = userRepo.findByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User "+user.chatId()+" does not exist"));
            List<OrmLink> linksToCheck = existingUser.subscriptions().stream()
                .map(OrmSubscription::link)
                .toList();
            userRepo.delete(existingUser);
            linksToCheck.stream()
                .filter(link -> link.subscriptions().isEmpty())
                .forEach(linkRepo::delete);
        } catch (DataAccessException e){
            throw new ScrapperOrmException("Error while deleting user with ORM", e);
        }
    }
}
