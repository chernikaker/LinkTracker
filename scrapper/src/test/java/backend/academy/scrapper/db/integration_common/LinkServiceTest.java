package backend.academy.scrapper.db.integration_common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class LinkServiceTest {

    public static final String PREFIX = "https://github.com/";
    public static final LinkType TYPE = LinkType.GITHUB;
    public static final LocalDateTime DT = LocalDateTime.now(ZoneId.systemDefault());
    public static final int BATCH_SIZE = 10;
    public static final int NO_CHECK_DURATION = 1000;
    public static final Link LINK = new Link(
            "https://github.com/1",
            LinkType.GITHUB,
            LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(2 * NO_CHECK_DURATION));

    @Autowired
    private LinkService linkService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getLinksToCheck_LinksPresent() {
        addLink(LINK);

        Map<Long, Link> links = assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, 0, NO_CHECK_DURATION));

        assertEquals(1, links.size());
        assertEquals(LINK.url(), links.entrySet().iterator().next().getValue().url());
    }

    @Test
    public void getLinksToCheck_OffsetEnded() {
        Link l = new Link(
                "https://github.com/1",
                LinkType.GITHUB,
                LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(2 * NO_CHECK_DURATION));
        addLink(l);

        Map<Long, Link> links =
                assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, BATCH_SIZE, NO_CHECK_DURATION));

        assertTrue(links.isEmpty());
    }

    @Test
    public void getLinksToCheck_NoLinksNeedCheck() {
        Link l = new Link("https://github.com/1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        addLink(l);

        Map<Long, Link> links = assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, 0, NO_CHECK_DURATION));

        assertTrue(links.isEmpty());
    }

    @Test
    public void getLinksToCheck_CorrectBatching() {
        for (int i = 1; i <= 15; i++) {
            Link l = new Link(PREFIX + i, TYPE, DT.minusSeconds(2 * NO_CHECK_DURATION));
            addLink(l);
        }
        Map<Long, Link> links = assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, 0, NO_CHECK_DURATION));
        assertEquals(BATCH_SIZE, links.size());
        links = assertDoesNotThrow(() -> linkService.getLinksToCheck(BATCH_SIZE, BATCH_SIZE, NO_CHECK_DURATION));
        assertEquals(5, links.size());
    }

    @Test
    @SneakyThrows
    public void updateValidationOnCurrTime_Success() {
        Long id = addLink(LINK);
        Thread.sleep(1000);

        assertDoesNotThrow(() -> linkService.updateLinkValidationOnCurrentTime(id));

        LocalDateTime curr = getDateTimeByLinkId(id);
        assertTrue(curr.isAfter(DT));
    }

    private Long addLink(Link l) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO link (url, last_validation) VALUES (?, ?) RETURNING id",
                Long.class,
                l.url(),
                l.lastValidation());
    }

    private LocalDateTime getDateTimeByLinkId(Long linkId) {
        return jdbcTemplate.queryForObject("SELECT last_validation FROM link WHERE id=?", LocalDateTime.class, linkId);
    }
}
