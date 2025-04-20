package backend.academy.scrapper.db.sql.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.db.exception.TestDataAccessException;
import backend.academy.scrapper.db.sql.entity.SqlLink;
import backend.academy.scrapper.db.sql.repository.LinkSqlRepository;
import backend.academy.scrapper.db.sql.service.SqlLinkService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.db.ScrapperSqlException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlLinkServiceUnitTest {

    private static final int BATCH_SIZE = 10;
    private static final long OFFSET = 0;
    private static final long DURATION_SECONDS = 3600;
    private static final LocalDateTime TEST_TIME = LocalDateTime.now(ZoneId.systemDefault());

    @Mock
    private LinkSqlRepository linkRepo;

    @InjectMocks
    private SqlLinkService linkService;

    @Test
    public void getLinksToCheck_linksPresent() {
        SqlLink sqlLink = new SqlLink(1L, "https://github.com/1", TEST_TIME.minusHours(2));
        when(linkRepo.getUncheckedLinksWithBatching(eq(BATCH_SIZE), eq(OFFSET), any(LocalDateTime.class)))
                .thenReturn(List.of(sqlLink));

        Map<Long, Link> result =
                assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, OFFSET, DURATION_SECONDS));
        assertEquals(1, result.size());
        assertEquals("https://github.com/1", result.get(1L).url());
    }

    @Test
    public void getLinksToCheck_noLinks() {
        when(linkRepo.getUncheckedLinksWithBatching(eq(BATCH_SIZE), eq(OFFSET), any(LocalDateTime.class)))
                .thenReturn(List.of());

        Map<Long, Link> result =
                assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, OFFSET, DURATION_SECONDS));
        assertEquals(0, result.size());
    }

    @Test
    void getLinksToCheck_ShouldThrowScrapperSqlExceptionOnError() {
        when(linkRepo.getUncheckedLinksWithBatching(anyInt(), anyLong(), any()))
                .thenThrow(new TestDataAccessException("error"));

        assertThatThrownBy(() -> linkService.getLinksToCheck(10, 0, 3600))
                .isInstanceOf(ScrapperSqlException.class)
                .hasMessageContaining("Error while getting links to check");
    }

    @Test
    void updateLinkValidationOnTime_ShouldCallRepositoryWithCurrentTime() {
        long linkId = 1L;
        LocalDateTime beforeCall = LocalDateTime.now(ZoneId.systemDefault());

        linkService.updateLinkValidationOnTime(linkId, beforeCall);

        verify(linkRepo).updateLinkValidationById(eq(linkId), argThat(time -> time.equals(beforeCall)));
    }

    @Test
    void updateLinkValidationOnTime_ShouldThrowScrapperSqlExceptionOnError() {
        long linkId = 1L;
        doThrow(new TestDataAccessException("error")).when(linkRepo).updateLinkValidationById(anyLong(), any());

        assertThatThrownBy(
                        () -> linkService.updateLinkValidationOnTime(linkId, LocalDateTime.now(ZoneId.systemDefault())))
                .isInstanceOf(ScrapperSqlException.class)
                .hasMessageContaining("Error while updating link validation");
    }
}
