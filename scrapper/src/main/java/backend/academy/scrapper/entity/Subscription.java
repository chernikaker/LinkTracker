package backend.academy.scrapper.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Subscription(User user, Link link, List<String> tags, List<String> filters) { }
