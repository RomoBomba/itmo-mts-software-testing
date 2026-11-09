package ru.itmo.testing.local;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class LocalApiServer {

    public static void main(String[] args) throws IOException {
        var server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080), 0
        );
        var executor = Executors.newFixedThreadPool(8);

        server.setExecutor(executor);
        server.createContext(
                "/api/products/", LocalApiServer::handleProduct
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            executor.shutdown();
        }));

        server.start();

        System.out.println("Local API started on http://127.0.0.1:8080");
        System.out.println("Example: http://127.0.0.1:8080/api/products/1");
    }

    private static void handleProduct(HttpExchange exchange)
            throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (!path.matches("/api/products/[1-5]")) {
            sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
            return;
        }

        int id = Integer.parseInt(
                path.substring(path.lastIndexOf('/') + 1)
        );

        String json = """
                {"id":%d,"name":"Product %d","price":%d}
                """.formatted(id, id, id * 100);

        sendJson(exchange, 200, json);
    }

    private static void sendJson(
            HttpExchange exchange, int status, String json
    ) throws IOException {
        try {
            byte[] body = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=UTF-8"
            );
            exchange.sendResponseHeaders(status, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        } finally {
            exchange.close();
        }
    }
}