package at.fhtw.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class RegisterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Nur zur Kontrolle, welche HTTP-Methode ankommt (GET, POST, etc.)
        String method = exchange.getRequestMethod();

        String response = "Register endpoint reached with method: " + method;

        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
