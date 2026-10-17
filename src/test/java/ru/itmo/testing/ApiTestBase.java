package ru.itmo.testing;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.config.RestAssuredConfig.config;

public abstract class ApiTestBase {

    protected RequestSpecification playgroundSpec;
    protected RequestSpecification graphqlSpec;

    @BeforeEach
    void setUpApi() {
        playgroundSpec = createSpecification(
                "https://playground.nileslabs.com/api/v1"
        ).filter(new CookieFilter());

        graphqlSpec = createSpecification(
                "https://graphql.postman-echo.com"
        );
    }

    private RequestSpecification createSpecification(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(config().logConfig(
                        logConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()
                ))
                .build();
    }
}