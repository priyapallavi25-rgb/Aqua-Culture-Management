import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/api/signup", exchange -> {

            // Allow website requests
            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Origin", "*");

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Methods", "POST, OPTIONS");

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Headers", "Content-Type");

            // Handle browser preflight requests
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405,
                        "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            try {
                // Read form data
                String body = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8);

                String username = getValue(body, "username");
                String email = getValue(body, "email");
                String password = getValue(body, "password");

                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    sendResponse(exchange, 400,
                            "{\"success\":false,\"message\":\"All fields are required\"}");
                    return;
                }

                // Register user in MySQL
                boolean registered =
                        Auth.register(username, email, password);

                if (registered) {
                    sendResponse(exchange, 200,
                            "{\"success\":true,\"message\":\"Account created successfully\"}");
                } else {
                    sendResponse(exchange, 400,
                            "{\"success\":false,\"message\":\"Username or email already exists, or registration failed\"}");
                }

            } catch (Exception e) {
                e.printStackTrace();

                sendResponse(exchange, 500,
                        "{\"success\":false,\"message\":\"Server error\"}");
            }
        });
        
        server.createContext("/api/login", exchange -> {

                exchange.getResponseHeaders()
                        .set("Access-Control-Allow-Origin", "*");

                exchange.getResponseHeaders()
                        .set("Access-Control-Allow-Methods", "POST, OPTIONS");

                exchange.getResponseHeaders()
                        .set("Access-Control-Allow-Headers", "Content-Type");

                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    sendResponse(exchange, 405,
                            "{\"success\":false,\"message\":\"Method not allowed\"}");
                    return;
                }

                try {

                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            java.nio.charset.StandardCharsets.UTF_8
                    );

                    String email = getValue(body, "email");
                    String password = getValue(body, "password");

                    boolean loggedIn = Auth.login(email, password);

                    if (loggedIn) {
                        sendResponse(exchange, 200,
                                "{\"success\":true,\"message\":\"Login successful\"}");
                    } else {
                        sendResponse(exchange, 401,
                                "{\"success\":false,\"message\":\"Invalid email or password\"}");
                    }

                } catch (Exception e) {

                    e.printStackTrace();

                    sendResponse(exchange, 500,
                            "{\"success\":false,\"message\":\"Server error\"}");
                }
            });
        server.start();

        System.out.println("AQUACULTURE SERVER STARTED");
        System.out.println("Signup API: http://localhost:8080/api/signup");
    }

    // Extract values from form data
    private static String getValue(String body, String key) {

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2 &&
                    parts[0].equals(key)) {

                return URLDecoder.decode(
                        parts[1], StandardCharsets.UTF_8);
            }
        }

        return "";
    }

    // Send response to website
    private static void sendResponse(
            HttpExchange exchange, int status, String response)
            throws IOException {

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
