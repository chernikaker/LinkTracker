package backend.academy.scrapper.client.github;

/** Контракт клиента Github Имеет единственный метод GET, возвращает текст JSON файла */
public interface GithubClient {

    String getResponse(String uri);
}
