package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.custom.repository.ScrapperLinkNotExistsException;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryLinkRepository {

    private static AtomicLong ID = new AtomicLong(0);
    private final Map<Long, Link> links = new HashMap<>();

    public long addLink(Link link) {
        long linkId = getLinkIdByURL(link.url());
        if (linkId != -1) {
            return linkId;
        }
        long newLinkId = ID.incrementAndGet();
        links.put(newLinkId, link);
        return newLinkId;
    }

    public void removeLinkById(long linkId) {
        if (links.remove(linkId) != null) {
            throw new ScrapperLinkNotExistsException("Link " + linkId + " does not exist");
        }
    }

    private long getLinkIdByURL(String url) {
        for(Map.Entry<Long, Link> entry : links.entrySet()) {
            if(entry.getValue().url().equals(url)) {
                return entry.getKey();
            }
        }
        return -1;
    }

}
