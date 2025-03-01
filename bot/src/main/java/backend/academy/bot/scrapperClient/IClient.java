package backend.academy.bot.scrapperClient;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;

public interface IClient {

    void registerChat(long userId);

    ListLinksResponse getUserLinks(long userId);

    LinkResponse addLinkSubscription(long userId, AddLinkRequest request);

    void deleteLinkSubscription(long userId, RemoveLinkRequest request);
}
