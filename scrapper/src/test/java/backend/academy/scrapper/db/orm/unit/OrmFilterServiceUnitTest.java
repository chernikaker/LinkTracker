package backend.academy.scrapper.db.orm.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.orm.entity.OrmFilter;
import backend.academy.scrapper.db.orm.entity.OrmSubscription;
import backend.academy.scrapper.db.orm.entity.OrmUser;
import backend.academy.scrapper.db.orm.repository.OrmFilterRepository;
import backend.academy.scrapper.db.orm.service.OrmFilterService;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrmFilterServiceUnitTest {

    private static final long ID = 1L;
    private static final OrmSubscription SUBSCRIPTION = new OrmSubscription();
    private static final OrmUser USER = new OrmUser();
    private static final OrmFilter TEST_FILTER = new OrmFilter(1L, "key1", "value1", SUBSCRIPTION, USER);
    private static final List<OrmFilter> TEST_FILTERS = List.of(TEST_FILTER);

    @Mock
    private OrmFilterRepository filterRepo;

    @InjectMocks
    private OrmFilterService filterService;

    @Test
    public void getFiltersBySubscriptionId_HasFilters() {
        when(filterRepo.getAllBySubscription_Id(ID)).thenReturn(TEST_FILTERS);

        List<Filter> result = assertDoesNotThrow(() -> filterService.getFiltersBySubscriptionId(ID));

        assertEquals(1, result.size());
        assertEquals("key1", result.getFirst().key());
        assertEquals("value1", result.getFirst().value());
    }

    @Test
    public void getFiltersBySubscriptionId_EmptyFilters() {
        when(filterRepo.getAllBySubscription_Id(ID)).thenReturn(List.of());

        List<Filter> result = assertDoesNotThrow(() -> filterService.getFiltersBySubscriptionId(ID));

        assertTrue(result.isEmpty());
    }

    @Test
    public void getFiltersBySubscriptionId_DataAccessException() {
        when(filterRepo.getAllBySubscription_Id(ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> filterService.getFiltersBySubscriptionId(ID)).isInstanceOf(ScrapperOrmException.class);
    }
}
