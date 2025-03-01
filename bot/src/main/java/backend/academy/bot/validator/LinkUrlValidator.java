package backend.academy.bot.validator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkUrlValidator {

    private static final String GITHUB_REPO_REGEX = "^https?://github\\.com/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+$";
    private static final String SO_QUESTION_REGEX_NAME = "^https?://stackoverflow\\.com/questions/\\d+(/[a-z0-9-]+)$";
    private static final String SO_QUESTION_REGEX_NO_NAME = "^https?://stackoverflow\\.com/questions/\\d+$";

    public boolean isValid(String link) {
        return link.matches(SO_QUESTION_REGEX_NAME)
                || link.matches(GITHUB_REPO_REGEX)
                || link.matches(SO_QUESTION_REGEX_NO_NAME);
    }
}
