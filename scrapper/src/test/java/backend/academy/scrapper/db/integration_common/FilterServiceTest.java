package backend.academy.scrapper.db.integration_common;

import backend.academy.scrapper.db.contract.FilterService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public abstract class FilterServiceTest {

    @Autowired
    private FilterService filterService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getFiltersBySubscriptionId_FiltersPresent(){
        Long subscriptionId = fillDataReturnSubId(true);

        List<Filter> f = assertDoesNotThrow(() -> filterService.getFiltersBySubscriptionId(subscriptionId));
        assertEquals(1, f.size());
    }

    @Test
    public void getFiltersBySubscriptionId_FiltersEmpty(){
        Long subscriptionId = fillDataReturnSubId(false);

        List<Filter> f = assertDoesNotThrow(() -> filterService.getFiltersBySubscriptionId(subscriptionId));
        assertEquals(0, f.size());
    }

    private Long fillDataReturnSubId(boolean withFilter) {
        Long userId = jdbcTemplate.queryForObject(
            "INSERT INTO tg_user (chat_id) VALUES (?) RETURNING id",
            Long.class, 1L);
        Long linkId = jdbcTemplate.queryForObject("INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
            Long.class, "https://github.com/1", LocalDateTime.now(ZoneId.systemDefault()));
        Long subscriptionId = jdbcTemplate.queryForObject("INSERT INTO subscription (user_id, link_id) VALUES(?,?) RETURNING id",
            Long.class, userId, linkId);
        if(withFilter){
            addFilters(subscriptionId, userId);
        }
        return subscriptionId;
    }

    private void addFilters(Long subscriptionId, Long userId){
        jdbcTemplate.update("INSERT INTO filter(key, value, subscription_id, user_id) VALUES(?,?,?,?)",
            "key", "value", subscriptionId, userId);
    }
}
