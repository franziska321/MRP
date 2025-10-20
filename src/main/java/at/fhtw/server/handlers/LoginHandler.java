package at.fhtw.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import at.fhtw.business.UserManager;

public class LoginHandler implements HttpHandler {

    private final UserManager userManager = new UserManager();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            System.out.println("LoginHandler wurde aufgerufen!");

            // JSON aus dem Request-Body lesen
            InputStream body = exchange.getRequestBody();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> request = mapper.readValue(body, Map.class);

            String username = request.get("username");
            String password = request.get("password");

            // Versuch, den User einzuloggen
            String token = userManager.loginUser(username, password);

            String response;
            int statusCode;

            if (token != null) {
                response = "Login successful. Token: " + token;
                statusCode = 200;
                System.out.println("Login erfolgreich für: " + username);
            } else {
                response = "Invalid username or password.";
                statusCode = 401;
                System.out.println("Login fehlgeschlagen für: " + username);
            }

            // Antwort senden
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        } else {
            exchange.sendResponseHeaders(405, -1); // Methode nicht erlaubt
        }
    }
}
