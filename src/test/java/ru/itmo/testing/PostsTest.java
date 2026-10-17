package ru.itmo.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class PostsTest extends ApiTestBase {

    @Test
    @DisplayName("Изменение заголовка поста сохраняет его id и содержимое")
    void shouldUpdateTitleAndPreservePostData() {
        String postId = given()
                .spec(playgroundSpec)
                .body("""
                        {
                          "title": "API testing post",
                          "body": "Initial post body",
                          "user_id": 1
                        }
                        """)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("title", equalTo("API testing post"))
                .body("body", equalTo("Initial post body"))
                .body("user_id", equalTo(1))
                .extract()
                .path("id");

        given()
                .spec(playgroundSpec)
                .pathParam("id", postId)
                .body("""
                        {
                          "title": "Updated API testing post"
                        }
                        """)
                .when()
                .patch("/posts/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("title", equalTo("Updated API testing post"));

        given()
                .spec(playgroundSpec)
                .pathParam("id", postId)
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("title", equalTo("Updated API testing post"))
                .body("body", equalTo("Initial post body"))
                .body("user_id", equalTo(1));
    }
}