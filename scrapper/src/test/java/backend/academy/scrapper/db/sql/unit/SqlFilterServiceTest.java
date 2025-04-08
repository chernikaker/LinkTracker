package backend.academy.scrapper.db.sql.unit;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.sql.entity.SqlFilter;
import backend.academy.scrapper.db.sql.repository.FilterSqlRepository;
import backend.academy.scrapper.db.sql.service.SqlFilterService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SqlFilterServiceTest {

    private final static SqlFilter FILTER = new SqlFilter(1L, "key", "value", 2L, 3L);

    @Mock
    private FilterSqlRepository filterRepo;

    @InjectMocks
    private SqlFilterService sqlFilterService;


    @Test
    public void getFiltersBySubscriptionId_filtersPresent() {
        long subscriptionId = 2L;

        when(filterRepo.getFiltersBySubscriptionId(subscriptionId)).thenReturn(List.of(FILTER));

        List<Filter> filters = assertDoesNotThrow(() -> sqlFilterService.getFiltersBySubscriptionId(subscriptionId));
        assertEquals(1, filters.size());
        assertEquals(FILTER.key(), filters.getFirst().key());
        assertEquals(FILTER.value(), filters.getFirst().value());
    }

    @Test
    public void getFiltersBySubscriptionId_filtersEmpty() {
        long subscriptionId = 2L;

        when(filterRepo.getFiltersBySubscriptionId(subscriptionId)).thenReturn(List.of());

        List<Filter> filters = assertDoesNotThrow(() -> sqlFilterService.getFiltersBySubscriptionId(subscriptionId));
        assertTrue(filters.isEmpty());
    }

    @Test
    public void getFiltersBySubscriptionId_throwsError() {
        long subscriptionId = 2L;

        when(filterRepo.getFiltersBySubscriptionId(subscriptionId)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> sqlFilterService.getFiltersBySubscriptionId(subscriptionId))
            .isInstanceOf(ScrapperSqlException.class)
            .hasMessageContaining("Error while getting filters");
    }
}
