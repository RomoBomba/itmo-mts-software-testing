package ru.itmo.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;

public class GraphQlTest extends ApiTestBase {

    @Test
    @DisplayName("GraphQL query возвращает приветствие для указанного имени")
    void shouldReturnGreetingForPerson() {
        given()
                .spec(graphqlSpec)
                .body("""
                        {
                          "query": "query Hello { hello(person: { name: \\"Ivan\\", age: 35 }) }"
                        }
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("$", not(hasKey("errors")))
                .body("data", notNullValue())
                .body("data.hello", containsString("Ivan"));
    }

    @Test
    @DisplayName("GraphQL mutation создаёт Person с указанными данными")
    void shouldCreatePerson() {
        given()
                .spec(graphqlSpec)
                .body("""
                        {
                          "query": "mutation CreatePerson { createPerson(person: { name: \\"Student\\", age: 20 }) { id name age } }"
                        }
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("$", not(hasKey("errors")))
                .body("data.createPerson", notNullValue())
                .body("data.createPerson.id", notNullValue())
                .body("data.createPerson.name", equalTo("Student"))
                .body("data.createPerson.age", equalTo(20));
    }
}