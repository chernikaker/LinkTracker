package backend.academy.scrapper.entity;

import java.util.Arrays;
import java.util.Objects;

public record Subscription(User user, Link link, String[] tags, String[] filters) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(user, that.user) &&
            Objects.equals(link, that.link) &&
            Arrays.equals(tags, that.tags) &&
            Arrays.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(user, link);
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + Arrays.hashCode(filters);
        return result;
    }
}
