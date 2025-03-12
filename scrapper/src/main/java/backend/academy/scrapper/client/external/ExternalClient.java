package backend.academy.scrapper.client.external;

/** Контракт клиента некоторого внешнего сервиса. Имеет единственный метод GET, возвращает текст JSON файла */
public interface ExternalClient {

    String getResponse(String uri);
}
