package backend.academy.scrapper.db.orm.unit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.orm.entity.OrmLink;
import backend.academy.scrapper.db.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.db.orm.service.OrmLinkService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.db.ScrapperOrmException;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class OrmLinkServiceUnitTest {

    private static final long LINK_ID = 1L;
    private static final long NON_EXISTENT_LINK_ID = 999L;
    private static final int BATCH_SIZE = 10;
    private static final long OFFSET = 0;
    private static final long DURATION_SECONDS = 3600;
    private static final String url = "https://github.com/";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final LocalDateTime OLD_VALIDATION_TIME = NOW.minusDays(1);

    @Mock
    private OrmLinkRepository linkRepo;

    @InjectMocks
    private OrmLinkService linkService;

    private OrmLink oldLink;

    @BeforeEach
    void setUp() {
        oldLink = new OrmLink(LINK_ID, url + "1", OLD_VALIDATION_TIME, List.of());
    }

    @Test
    public void updateLinkValidationOnTime_Success() {
        when(linkRepo.findById(LINK_ID)).thenReturn(Optional.of(oldLink));

        assertDoesNotThrow(() -> linkService.updateLinkValidationOnTime(LINK_ID, NOW));

        assertThat(oldLink.lastValidation()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
        verify(linkRepo).save(oldLink);
        verify(linkRepo).flush();
    }

    @Test
    public void updateLinkValidationOnCurrentTime_LinkNotFoundException() {
        when(linkRepo.findById(NON_EXISTENT_LINK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.updateLinkValidationOnTime(NON_EXISTENT_LINK_ID, NOW))
                .isInstanceOf(ScrapperLinkNotExistsException.class);
        verify(linkRepo, never()).save(any());
        verify(linkRepo, never()).flush();
    }

    @Test
    public void updateLinkValidationOnCurrentTime_DataAccessError() {
        when(linkRepo.findById(LINK_ID)).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> linkService.updateLinkValidationOnTime(LINK_ID, NOW))
                .isInstanceOf(ScrapperOrmException.class);
    }

    @Test
    public void getLinksToCheck_returnsLinksSuccessfully() {
        Pageable pageable = PageRequest.of((int) (OFFSET / BATCH_SIZE), BATCH_SIZE);

        when(linkRepo.findUncheckedLinks(any(), eq(pageable))).thenReturn(List.of(oldLink));

        Map<Long, Link> result =
                assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, OFFSET, DURATION_SECONDS));

        assertThat(result).hasSize(1);
        assertThat(result).containsOnlyKeys(LINK_ID);
    }

    @Test
    public void getLinksToCheck_throwsOnDatabaseError() {
        when(linkRepo.findUncheckedLinks(any(), any())).thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> linkService.getLinksToCheck(BATCH_SIZE, OFFSET, DURATION_SECONDS))
                .isInstanceOf(ScrapperOrmException.class);
    }
}
