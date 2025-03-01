package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.dto.GithubInfo;
import java.util.List;

public interface GithubClient {

    String getCommits(String uri);
    String getComments(String uri);
    String getIssues(String uri);
}
