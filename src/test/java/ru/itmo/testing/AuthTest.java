package ru.itmo.testing;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class AuthTest extends ApiTestBase {

    @Test
    @DisplayName("Получение токена и доступ к профилю пользователя")
    void shouldLoginAndGetCurrentUser() {
        Response loginResponse = given()
                .spec(playgroundSpec)
                .body("""
                        {
                          "username": "Bret",
                          "password": "Password@123"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("access_token", not(emptyOrNullString()))
                .body("user.id", notNullValue())
                .body("user.username", equalToIgnoringCase("Bret"))
                .body("user.email", not(emptyOrNullString()))
                .extract()
                .response();

        String accessToken = loginResponse.path("access_token");
        Object expectedUserId = loginResponse.path("user.id");
        String expectedUsername = loginResponse.path("user.username");
        String expectedEmail = loginResponse.path("user.email");

        given()
                .spec(playgroundSpec)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(200)
                .body("id", equalTo(expectedUserId))
                .body("username", equalTo(expectedUsername))
                .body("email", equalTo(expectedEmail));
    }

    @Test
    @DisplayName("Запрос профиля без токена отклоняется")
    void shouldRejectRequestWithoutToken() {
        given()
                .spec(playgroundSpec)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401);
    }

    @ParameterizedTest(name = "Вариант {index}: неверный пароль")
    @ValueSource(strings = {"Password@1234", "password@123"})
    @DisplayName("Вход с неверным паролем отклоняется")
    void shouldRejectLoginWithIncorrectPassword(String password) {
        given()
                .spec(playgroundSpec)
                .body("""
                        {
                          "username": "Bret",
                          "password": "%s"
                        }
                        """.formatted(password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("error", equalTo("Invalid username or password."))
                .body("$", not(hasKey("access_token")))
                .body("$", not(hasKey("refresh_token")));
    }
}
