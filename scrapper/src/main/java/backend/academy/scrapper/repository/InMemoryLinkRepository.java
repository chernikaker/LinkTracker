package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.repository.ScrapperLinkNotExistsException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLinkRepository {

    private final AtomicLong ID = new AtomicLong(0);
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

    public Set<Link> getLinks() {
        return new HashSet<>(links.values());
    }

    public void removeLinkById(long linkId) {
        if (links.remove(linkId) == null) {
            throw new ScrapperLinkNotExistsException("Link " + linkId + " does not exist");
        }
    }

    public long getLinkIdByURL(String url) {
        for (Map.Entry<Long, Link> entry : links.entrySet()) {
            if (entry.getValue().url().equals(url)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public boolean containsLink(long linkId) {
        return links.containsKey(linkId);
    }

    public void clear() {
        links.clear();
        ID.set(0);
    }
}
