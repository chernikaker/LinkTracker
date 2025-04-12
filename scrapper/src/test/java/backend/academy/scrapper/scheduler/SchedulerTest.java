package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.client.bot.BotClientService;
import backend.academy.scrapper.client.external.github.GithubClientService;
import backend.academy.scrapper.client.external.stackoverflow.StackoverflowClientService;
import backend.academy.scrapper.db.contract.LinkService;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.model.UpdateInfo;
import backend.academy.scrapper.model.UpdateInfoType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {

    @Mock
    private LinkService linkService;

    @Mock
    private GithubClientService githubClientService;

    @Mock
    private StackoverflowClientService soClientService;

    @Mock
    private BotClientService botClientService;

    private UpdateScheduler scheduler;

    private Link githubLink;
    private Link stackoverflowLink;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new UpdateScheduler(linkService, githubClientService, soClientService, botClientService, 60L, 100);
        githubLink = new Link("https://github.com/mock", LinkType.GITHUB, LocalDateTime.of(2023, 10, 1, 12, 0));
        stackoverflowLink = new Link(
                "https://stackoverflow.com/mock", LinkType.STACKOVERFLOW, LocalDateTime.of(2023, 10, 1, 12, 0));
    }

    @Test
    public void checkNewUpdates_noLinkUpdates() {
        when(linkService.getLinksToCheck(anyInt(), anyLong(), anyLong()))
            .thenReturn(Map.of());

        scheduler.checkNewUpdates();

        verify(botClientService, never()).sendUpdates(anyLong(), any(), any());
        verify(githubClientService, never()).getAllInfo(any());
        verify(soClientService, never()).getAllInfo(any());
    }

    @Test
    public void checkNewUpdates_manyLinksUpdates() {
        LocalDateTime linkUpdateTime1 = githubLink.lastValidation();
        LocalDateTime linkUpdateTime2 = stackoverflowLink.lastValidation();
        when(linkService.getLinksToCheck(anyInt(), anyLong(), anyLong()))
            .thenReturn(Map.of(1L, githubLink, 2L, stackoverflowLink))
            .thenReturn(Map.of());
        doAnswer(i -> githubLink.lastValidation(LocalDateTime.now())).when(linkService).updateLinkValidationOnCurrentTime(1L);
        doAnswer(i -> stackoverflowLink.lastValidation(LocalDateTime.now())).when(linkService).updateLinkValidationOnCurrentTime(2L);
        UpdateInfo updateInfo1 = new UpdateInfo("New pr","description", "Author", linkUpdateTime1.plusDays(1), UpdateInfoType.PULL_REQUEST);
        UpdateInfo updateInfo2 =
            new UpdateInfo("New answer","description", "Author", linkUpdateTime2.plusDays(1), UpdateInfoType.ANSWER);
        when(githubClientService.getAllInfo(githubLink)).thenReturn(List.of(updateInfo1));
        when(soClientService.getAllInfo(stackoverflowLink)).thenReturn(List.of(updateInfo2));

        scheduler.checkNewUpdates();

        verify(botClientService).sendUpdates(1L, githubLink.url(), List.of(updateInfo1));
        verify(botClientService).sendUpdates(2L, stackoverflowLink.url(), List.of(updateInfo2));
        assertTrue(githubLink.lastValidation().isAfter(linkUpdateTime1));
        assertTrue(stackoverflowLink.lastValidation().isAfter(linkUpdateTime2));
    }

    @Test
    public void checkNewUpdates_manyLinksOneUpdateOnlyOneSent() {
        LocalDateTime linkUpdateTime1 = githubLink.lastValidation();
        LocalDateTime linkUpdateTime2 = stackoverflowLink.lastValidation();
        when(linkService.getLinksToCheck(anyInt(), anyLong(), anyLong()))
            .thenReturn(Map.of(1L, githubLink, 2L, stackoverflowLink))
            .thenReturn(Map.of());
        doAnswer(i -> githubLink.lastValidation(LocalDateTime.now())).when(linkService).updateLinkValidationOnCurrentTime(1L);
        doAnswer(i -> stackoverflowLink.lastValidation(LocalDateTime.now())).when(linkService).updateLinkValidationOnCurrentTime(2L);
        UpdateInfo updateInfo1 = new UpdateInfo("New pr","description", "Author", linkUpdateTime1.plusDays(1), UpdateInfoType.PULL_REQUEST);
        UpdateInfo updateInfo2 =
                new UpdateInfo("New answer","description", "Author", linkUpdateTime2.minusDays(1), UpdateInfoType.ANSWER);
        when(githubClientService.getAllInfo(any())).thenReturn(List.of(updateInfo1));
        when(soClientService.getAllInfo(any())).thenReturn(List.of(updateInfo2));

        scheduler.checkNewUpdates();

        verify(botClientService).sendUpdates(1L, githubLink.url(), List.of(updateInfo1));
        verify(botClientService, times(1)).sendUpdates(anyLong(), any(), any());
    }
}
