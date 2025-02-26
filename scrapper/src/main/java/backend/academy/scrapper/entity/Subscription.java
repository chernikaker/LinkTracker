package backend.academy.scrapper.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Subscription(long userId, User user, long linkId, Link link, List<String> tags, List<String> filters) { }
