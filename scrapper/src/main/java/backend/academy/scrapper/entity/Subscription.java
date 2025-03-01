package backend.academy.scrapper.entity;

import java.util.List;

public record Subscription(long userId, User user, long linkId, Link link, List<String> tags, List<String> filters) {}
