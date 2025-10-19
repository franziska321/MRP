package at.fhtw.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "Login endpoint reached successfully!";

        // Header setzen und senden
        exchange.sendResponseHeaders(200, response.getBytes().length);

        // Antwort in den OutputStream schreiben
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
