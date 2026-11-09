package ru.itmo.testing.load;

import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class ProductsSimulation extends Simulation {

    {
        int users = Integer.getInteger("users", 10);
        int rampup = Integer.getInteger("rampup", 10);
        int duration = Integer.getInteger("duration", 120);

        var protocol = http
                .baseUrl("http://127.0.0.1:8080")
                .disableCaching()
                .disableWarmUp();

        var products = csv("products.csv").circular();

        var scenario = scenario("Products")
                .forever().on(
                        feed(products)
                                .pause(1)
                                .exec(
                                        http("Get product")
                                                .get("/api/products/#{id}")
                                                .check(status().is(200))
                                )
                );

        setUp(
                scenario.injectClosed(
                        rampConcurrentUsers(0)
                                .to(users)
                                .during(Duration.ofSeconds(rampup)),
                        constantConcurrentUsers(users)
                                .during(Duration.ofSeconds(duration - rampup))
                )
        )
                .protocols(protocol)
                .maxDuration(Duration.ofSeconds(duration));
    }
}