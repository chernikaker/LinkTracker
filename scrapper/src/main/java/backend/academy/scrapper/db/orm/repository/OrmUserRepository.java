package backend.academy.scrapper.db.orm.repository;

import backend.academy.scrapper.db.orm.entity.OrmUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmUserRepository extends JpaRepository<OrmUser, Long> {

    Optional<OrmUser> findByChatId(Long chatId);
}
