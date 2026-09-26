package ru.mts.itmo.aqa.junit5;

import io.qameta.allure.Attachment;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.BLACKLISTED;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.CONTAINS_LOGIN;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.HAS_WHITESPACE;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_DIGIT;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_LOWER;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_SPECIAL;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_UPPER;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.REPEATED_RUN;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.TOO_LONG;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.TOO_SHORT;

@Epic("Политика паролей")
@Feature("Проверка PasswordPolicy по спецификации")
@DisplayName("PasswordPolicy: проверка пароля по спецификации")
class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    @Nested
    @Story("Общий результат проверки")
    @DisplayName("Общее поведение")
    class GeneralBehavior {

        @Test
        @DisplayName("Валидный пароль не содержит нарушений")
        void validPasswordHasNoViolations() {
            PasswordPolicy.Result result = checkPassword("Aa1!bcde");

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(result.isValid()).isTrue();
            softly.assertThat(result.violations()).isEmpty();
            softly.assertAll();
        }

        @Test
        @DisplayName("Все нарушения накапливаются одновременно")
        void accumulatesSeveralViolations() {
            PasswordPolicy.Result result = checkPassword("aaaa ");

            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).containsExactlyInAnyOrder(
                    TOO_SHORT, NO_DIGIT, NO_UPPER, NO_SPECIAL, HAS_WHITESPACE, REPEATED_RUN);
        }

        @Test
        @DisplayName("null вместо пароля вызывает IllegalArgumentException")
        void nullPasswordThrowsException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> policy.check(null))
                    .withMessageContaining("null");
        }
    }

    @Nested
    @Story("Границы длины")
    @DisplayName("Правила TOO_SHORT и TOO_LONG")
    class LengthRules {

        @ParameterizedTest(name = "длина {0}: ожидаемое нарушение {1}")
        @MethodSource("ru.mts.itmo.aqa.junit5.PasswordPolicyTest#lengthCases")
        @DisplayName("Границы 7/8 и 64/65")
        void checksLengthBoundaries(int length, PasswordPolicy.Violation expectedViolation) {
            PasswordPolicy.Result result = checkPassword(validPasswordOfLength(length));

            if (expectedViolation == null) {
                assertThat(result.violations()).doesNotContain(TOO_SHORT, TOO_LONG);
            } else {
                assertThat(result.violations()).contains(expectedViolation);
            }
            // REG-001: закрывает дефект об ошибочном TOO_LONG для длины 64.
        }
    }

    @Nested
    @Story("Обязательные группы символов")
    @DisplayName("Правила NO_DIGIT, NO_UPPER, NO_LOWER и NO_SPECIAL")
    class RequiredCharacters {

        @ParameterizedTest(name = "{0}")
        @MethodSource("ru.mts.itmo.aqa.junit5.PasswordPolicyTest#missingCharacterCases")
        @DisplayName("Нарушение срабатывает, если группа символов отсутствует")
        void reportsMissingCharacterGroup(String description, String password,
                                          PasswordPolicy.Violation violation) {
            assertThat(checkPassword(password).violations()).contains(violation);
        }

        @Test
        @DisplayName("Все обязательные группы присутствуют — нарушений нет")
        void doesNotReportPresentCharacterGroups() {
            assertThat(checkPassword("Aa1!bcde").violations())
                    .doesNotContain(NO_DIGIT, NO_UPPER, NO_LOWER, NO_SPECIAL);
        }
    }

    @Nested
    @Story("Пробельные символы")
    @DisplayName("Правило HAS_WHITESPACE")
    class WhitespaceRule {

        @ParameterizedTest(name = "пробельный символ {0}")
        @CsvSource(value = {"space|Aa1! bcde", "tab|Aa1!\tbcde"}, delimiter = '|')
        @DisplayName("Пробел или табуляция вызывают нарушение")
        void reportsWhitespace(String description, String password) {
            assertThat(checkPassword(password).violations()).contains(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("Пароль без пробельных символов не нарушает правило")
        void acceptsPasswordWithoutWhitespace() {
            assertThat(checkPassword("Aa1!bcde").violations()).doesNotContain(HAS_WHITESPACE);
        }
    }

    @Nested
    @Story("Повторяющиеся символы")
    @DisplayName("Правило REPEATED_RUN")
    class RepeatedRunRule {

        @ParameterizedTest(name = "серия из {0}: REPEATED_RUN = {1}")
        @CsvSource({"2, false", "3, true"})
        @DisplayName("Граница 2/3 одинаковых символа")
        void checksRepeatedRunBoundary(int runLength, boolean expected) {
            String password = "Ab1!cda" + "e".repeat(runLength);

            assertThat(checkPassword(password).violations().contains(REPEATED_RUN)).isEqualTo(expected);
            // REG-002: закрывает дефект о несрабатывании REPEATED_RUN для трёх символов.
        }
    }

    @Nested
    @Story("Содержание логина")
    @DisplayName("Правило CONTAINS_LOGIN")
    class LoginRule {

        @Test
        @DisplayName("Логин ищется без учёта регистра")
        void reportsContainedLoginIgnoringCase() {
            assertThat(checkPassword("Aa1!IVANxx", "ivan").violations()).contains(CONTAINS_LOGIN);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("ru.mts.itmo.aqa.junit5.PasswordPolicyTest#loginNotAppliedCases")
        @DisplayName("Правило не срабатывает без непустого логина")
        void doesNotApplyWithoutNonEmptyLogin(String description, String login) {
            assertThat(checkPassword("Aa1!ivanxx", login).violations()).doesNotContain(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("Непустой логин, которого нет в пароле, не даёт нарушение")
        void acceptsPasswordWithoutLoginSubstring() {
            assertThat(checkPassword("Aa1!bcde", "ivan").violations()).doesNotContain(CONTAINS_LOGIN);
        }
    }

    @Nested
    @Story("Чёрный список")
    @DisplayName("Правило BLACKLISTED")
    class BlacklistRule {

        @ParameterizedTest(name = "{0} входит в чёрный список")
        @MethodSource("ru.mts.itmo.aqa.junit5.PasswordPolicyTest#blacklistedPasswords")
        @DisplayName("Простые пароли запрещены без учёта регистра")
        void reportsBlacklistedPasswordIgnoringCase(String password) {
            assertThat(checkPassword(password).violations()).contains(BLACKLISTED);
            // REG-003: закрывает дефект о регистрозависимой проверке чёрного списка.
        }

        @Test
        @DisplayName("Пароль не из чёрного списка не нарушает правило")
        void acceptsPasswordOutsideBlacklist() {
            assertThat(checkPassword("Aa1!bcde").violations()).doesNotContain(BLACKLISTED);
        }
    }

    @Step("Проверить пароль: {password}")
    private PasswordPolicy.Result checkPassword(String password) {
        PasswordPolicy.Result result = policy.check(password);
        attachResult(password, result.violations());
        return result;
    }

    @Step("Проверить пароль {password} с логином {login}")
    private PasswordPolicy.Result checkPassword(String password, String login) {
        PasswordPolicy.Result result = policy.check(password, login);
        attachResult(password, result.violations());
        return result;
    }

    @Attachment(value = "Результат проверки", type = "text/plain")
    private String attachResult(String password, List<PasswordPolicy.Violation> violations) {
        return "password=" + password + System.lineSeparator() + "violations=" + violations;
    }

    private static Stream<Arguments> lengthCases() {
        return Stream.of(
                Arguments.of(7, TOO_SHORT),
                Arguments.of(8, null),
                Arguments.of(64, null),
                Arguments.of(65, TOO_LONG));
    }

    private static Stream<Arguments> missingCharacterCases() {
        return Stream.of(
                Arguments.of("нет цифры", "Abc!defg", NO_DIGIT),
                Arguments.of("нет заглавной буквы", "abc1!def", NO_UPPER),
                Arguments.of("нет строчной буквы", "ABC1!DEF", NO_LOWER),
                Arguments.of("нет спецсимвола", "Abc1defg", NO_SPECIAL));
    }

    private static Stream<Arguments> loginNotAppliedCases() {
        return Stream.of(
                Arguments.of("логин пустой", ""),
                Arguments.of("логин не передан", null));
    }

    private static Stream<String> blacklistedPasswords() {
        return Stream.of("password", "qwerty", "123456", "admin", "welcome", "PaSsWoRd");
    }

    private static String validPasswordOfLength(int length) {
        StringBuilder password = new StringBuilder("Aa1!");
        for (int i = password.length(); i < length; i++) {
            password.append(i % 2 == 0 ? 'b' : 'c');
        }
        return password.toString();
    }
}
