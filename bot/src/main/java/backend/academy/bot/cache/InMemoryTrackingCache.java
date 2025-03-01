package backend.academy.bot.cache;

import backend.academy.bot.model.LinkTrackingObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InMemoryTrackingCache {

    private final Map<Long, LinkTrackingObject> tracks = new HashMap<>();

    public boolean containsTrack(long id) {
        return tracks.containsKey(id);
    }

    public Optional<LinkTrackingObject> getTrack(long id) {
        if (tracks.containsKey(id)) {
            return Optional.of(tracks.get(id));
        } else {
            return Optional.empty();
        }
    }

    public void setTrack(long id, LinkTrackingObject track) {
        tracks.put(id, track);
    }

    public void removeTrack(long id) {
        tracks.remove(id);
    }
}
