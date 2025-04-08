package backend.academy.scrapper.db.orm.service;

import backend.academy.scrapper.db.contract.UserService;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.mapper.OrmUserMapper;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.repository.OrmUserRepository;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperUserAlreadyExistsException;
import backend.academy.scrapper.exception.repository.ScrapperUserNotExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
            userRepo.save(OrmUserMapper.mapToOrm(user));
        } catch (DataAccessException e){
            throw new ScrapperOrmException("Error while adding user with ORM", e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(User user) {
        try {
            OrmUser existingUser = userRepo.findByChatId(user.chatId())
                .orElseThrow(() -> new ScrapperUserNotExistsException("User " + user.chatId() + " does not exist"));
            List<OrmLink> linksToCheck = new ArrayList<>();
            if (existingUser.subscriptions() != null) {
                for (OrmSubscription s: existingUser.subscriptions()) {
                   linksToCheck.add(s.link());
                }
            }
            userRepo.delete(existingUser);
            userRepo.flush();
            for(OrmLink link: linksToCheck) {
                if(link.subscriptions().isEmpty()) {
                    linkRepo.delete(link);
                }
            }
            linkRepo.flush();
        } catch (DataAccessException e){
            throw new ScrapperOrmException("Error while deleting user with ORM", e);
        }
    }
}
