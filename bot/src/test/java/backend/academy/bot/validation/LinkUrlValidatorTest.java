package backend.academy.bot.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.bot.validator.LinkUrlValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LinkUrlValidatorTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://github.com/user/repo",
                "http://github.com/user/repo",
                "https://github.com/user-name/repo_name",
                "https://github.com/user123/repo123"
            })
    void testValidGitHubLinks(String link) {
        assertTrue(LinkUrlValidator.isValid(link));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://stackoverflow.com/questions/12345",
                "http://stackoverflow.com/questions/12345",
                "https://stackoverflow.com/questions/12345/title-of-question",
                "http://stackoverflow.com/questions/12345/title-of-question"
            })
    void testValidStackOverflowLinks(String link) {
        assertTrue(LinkUrlValidator.isValid(link));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://github.com/user",
                "https://github.com/user/repo/extra",
                "https://stackoverflow.com/questions",
                "https://stackoverflow.com/questions/",
                "https://stackoverflow.com/questions/abc",
                "https://example.com",
                "ftp://github.com/user/repo",
                "https://github.com/",
                "https://stackoverflow.com/questions/12345/",
                "https://stackoverflow.com/questions/12345//"
            })
    void testInvalidLinks(String link) {
        assertFalse(LinkUrlValidator.isValid(link));
    }

    @Test
    void testEmptyInput() {
        assertFalse(LinkUrlValidator.isValid(""));
    }
}
