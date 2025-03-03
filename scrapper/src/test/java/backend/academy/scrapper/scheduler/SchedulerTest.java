package backend.academy.scrapper.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.github.GithubClientService;
import backend.academy.scrapper.client.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {

    @Mock
    private InMemoryLinkRepository linkRepository;

    @Mock
    private GithubClientService githubClientService;

    @Mock
    private StackoverflowClientService soClientService;

    @Mock
    private BotClientService botClientService;

    @InjectMocks
    private SchedulerUpdateService schedulerUpdateService;

    @Captor
    private ArgumentCaptor<Link> linkCaptor;

    @Captor
    private ArgumentCaptor<List<UpdateInfo>> updatesCaptor;

    private Link githubLink;
    private Link stackoverflowLink;

    @BeforeEach
    public void setUp() {
        githubLink = new Link("https://github.com/mock", LinkType.GITHUB, LocalDateTime.of(2023, 10, 1, 12, 0));
        stackoverflowLink = new Link(
                "https://stackoverflow.com/mock", LinkType.STACKOVERFLOW, LocalDateTime.of(2023, 10, 1, 12, 0));
    }

    @Test
    public void checkNewUpdates_oneLinkUpdates() {
        LocalDateTime linkUpdateTime = githubLink.lastValidation();
        when(linkRepository.getLinks()).thenReturn(Set.of(githubLink));
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        UpdateInfo updateInfo = new UpdateInfo("New commit", "Author", now, UpdateInfoType.COMMIT);
        when(githubClientService.getAllInfo(githubLink)).thenReturn(List.of(updateInfo));

        schedulerUpdateService.checkNewUpdates();

        verify(botClientService).sendUpdates(linkCaptor.capture(), updatesCaptor.capture());
        List<UpdateInfo> capturedUpdates = updatesCaptor.getValue();
        assertEquals(1, capturedUpdates.size());
        assertEquals("New commit", capturedUpdates.getFirst().message());
        assertEquals("Author", capturedUpdates.getFirst().authorName());
        assertEquals(UpdateInfoType.COMMIT, capturedUpdates.getFirst().type());
        assertTrue(githubLink.lastValidation().isAfter(linkUpdateTime));
    }

    @Test
    public void checkNewUpdates_manyLinksUpdates() {
        LocalDateTime linkUpdateTime1 = githubLink.lastValidation();
        LocalDateTime linkUpdateTime2 = stackoverflowLink.lastValidation();
        when(linkRepository.getLinks()).thenReturn(Set.of(githubLink, stackoverflowLink));
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        UpdateInfo updateInfo1 = new UpdateInfo("New commit", "Author", now, UpdateInfoType.COMMIT);
        UpdateInfo updateInfo2 = new UpdateInfo("New answer", "Author", now, UpdateInfoType.ANSWER);
        when(githubClientService.getAllInfo(githubLink)).thenReturn(List.of(updateInfo1));
        when(soClientService.getAllInfo(stackoverflowLink)).thenReturn(List.of(updateInfo2));

        schedulerUpdateService.checkNewUpdates();

        verify(botClientService).sendUpdates(githubLink, List.of(updateInfo1));
        verify(botClientService).sendUpdates(stackoverflowLink, List.of(updateInfo2));
        assertTrue(githubLink.lastValidation().isAfter(linkUpdateTime1));
        assertTrue(stackoverflowLink.lastValidation().isAfter(linkUpdateTime2));
    }

    @Test
    public void checkNewUpdates_manyLinksOneUpdate() {
        LocalDateTime linkUpdateTime1 = githubLink.lastValidation();
        LocalDateTime linkUpdateTime2 = stackoverflowLink.lastValidation();
        when(linkRepository.getLinks()).thenReturn(Set.of(githubLink, stackoverflowLink));
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        UpdateInfo updateInfo1 = new UpdateInfo("New commit", "Author", now, UpdateInfoType.COMMIT);
        UpdateInfo updateInfo2 =
                new UpdateInfo("New answer", "Author", linkUpdateTime2.minusDays(1), UpdateInfoType.ANSWER);
        when(githubClientService.getAllInfo(githubLink)).thenReturn(List.of(updateInfo1));
        when(soClientService.getAllInfo(stackoverflowLink)).thenReturn(List.of(updateInfo2));

        schedulerUpdateService.checkNewUpdates();

        verify(botClientService).sendUpdates(githubLink, List.of(updateInfo1));
        verify(botClientService, times(1)).sendUpdates(any(), any());
        assertTrue(githubLink.lastValidation().isAfter(linkUpdateTime1));
        assertTrue(stackoverflowLink.lastValidation().isAfter(linkUpdateTime2));
    }
}
