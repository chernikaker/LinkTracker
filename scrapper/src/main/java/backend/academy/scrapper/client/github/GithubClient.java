package backend.academy.scrapper.client.github;

public interface GithubClient {

    String getCommits(String uri);
    String getComments(String uri);
    String getIssues(String uri);
}
