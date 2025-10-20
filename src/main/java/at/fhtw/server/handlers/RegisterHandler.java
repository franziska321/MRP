package at.fhtw.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import at.fhtw.business.UserManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class RegisterHandler implements HttpHandler {
    private final UserManager userManager = new UserManager();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStream body = exchange.getRequestBody();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> request = mapper.readValue(body, Map.class);



            String username = request.get("username");
            String password = request.get("password");

            boolean success = userManager.registerUser(username, password);

            String response;
            if (success) {
                // Token direkt zurückgeben
                response = "User registered successfully. Token: " + username + "_mrpToken";
                exchange.sendResponseHeaders(200, response.getBytes().length);
            } else {
                response = "Username already exists";
                exchange.sendResponseHeaders(409, response.getBytes().length);
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}

