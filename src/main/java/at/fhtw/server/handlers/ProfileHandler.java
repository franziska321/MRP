package at.fhtw.server.handlers;

import at.fhtw.models.User;
import at.fhtw.persistence.FavoritesRepository;
import at.fhtw.server.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileHandler implements HttpHandler {

    private final AuthService authService = new AuthService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (!authService.isAuthorized(authHeader)) {
            String response = "Unauthorized";
            exchange.sendResponseHeaders(401, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        // User anhand des Tokens abrufen
        User user = authService.getUserByToken(authHeader);

        if (user == null) {
            String response = "Unauthorized";
            exchange.sendResponseHeaders(401, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        FavoritesRepository favoritesRepo = new FavoritesRepository();
        List<Integer> favoriteMediaIds = favoritesRepo.getUserFavorites(user.getId());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", user.getId());
        responseData.put("username", user.getUsername());
        responseData.put("token", user.getToken());
        responseData.put("favorites", favoriteMediaIds);  // NEU: Favorites hinzufügen
        responseData.put("favoritesCount", favoriteMediaIds.size());  // Optional: Count

        String response = mapper.writeValueAsString(responseData);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }


    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        exchange.sendResponseHeaders(status, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
