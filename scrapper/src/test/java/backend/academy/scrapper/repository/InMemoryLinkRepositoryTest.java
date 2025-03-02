package backend.academy.scrapper.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryLinkRepositoryTest {

    private InMemoryLinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        linkRepository = new InMemoryLinkRepository();
    }

    @Test
    public void addNewLink() {
        Link link = new Link("url", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));

        long linkId = linkRepository.addLink(link);

        assertEquals(1, linkId);
    }

    @Test
    void addAlreadyExistingUrlLink() {

        Link link = new Link("url", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        Link copyLink = new Link("url", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        linkRepository.addLink(link);

        long linkId = linkRepository.addLink(copyLink);

        assertEquals(1, linkId);
    }

    @Test
    void getLinks_EmptySet() {
        Set<Link> links = linkRepository.getLinks();

        assertTrue(links.isEmpty());
    }

    @Test
    void getLinks_ContainsLinks() {
        Link link1 = new Link("url1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        Link link2 = new Link("url2", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        linkRepository.addLink(link1);
        linkRepository.addLink(link2);

        Set<Link> links = linkRepository.getLinks();

        assertEquals(2, links.size());
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
    }

    @Test
    void removeLinkById_correctId() {
        Link link = new Link("url1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        long linkId = linkRepository.addLink(link);

        linkRepository.removeLinkById(linkId);

        assertFalse(linkRepository.getLinks().contains(link));
    }

    @Test
    void removeLinkById_ShouldThrowException_WhenLinkDoesNotExist() {
        long nonExistentLinkId = 999L;

        assertThatThrownBy(() -> linkRepository.removeLinkById(nonExistentLinkId))
                .isInstanceOf(ScrapperLinkNotExistsException.class);
    }

    @Test
    void getLinkIdByURL_ShouldReturnLinkId_WhenLinkExists() {
        Link link = new Link("url1", LinkType.GITHUB, LocalDateTime.now(ZoneId.systemDefault()));
        long linkId = linkRepository.addLink(link);

        long foundLinkId = linkRepository.getLinkIdByURL("url1");

        assertEquals(linkId, foundLinkId);
    }

    @Test
    void getLinkIdByURL_ShouldReturnMinusOne_WhenLinkDoesNotExist() {
        long foundLinkId = linkRepository.getLinkIdByURL("url");

        assertEquals(-1, foundLinkId);
    }
}
