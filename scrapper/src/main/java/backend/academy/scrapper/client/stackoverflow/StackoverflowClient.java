package backend.academy.scrapper.client.stackoverflow;

/** Контракт клиента StackOverflow Имеет единственный метод GET, возвращает текст JSON файла */
public interface StackoverflowClient {

    String getResponse(String uri);
}
