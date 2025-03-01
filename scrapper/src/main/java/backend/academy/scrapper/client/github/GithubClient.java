package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.dto.GithubCommitInfo;
import backend.academy.scrapper.entity.Link;
import java.util.List;

public interface GithubClient {

    List<GithubCommitInfo> getCommits(Link link);
}
