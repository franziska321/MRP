package at.fhtw.server.handlers;

import at.fhtw.business.RatingManager;
import at.fhtw.server.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class RatingHandler implements HttpHandler {
    private final RatingManager ratingManager = new RatingManager();
    private final AuthService authService = new AuthService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // auth check
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!authService.isAuthorized(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        try {
            String[] pathParts = path.split("/");
            int mediaId = Integer.parseInt(pathParts[3]); // media/{id}/rate

            switch(method) {
                case "POST":
                    handleRateMedia(exchange, mediaId, authHeader);
                    break;
                case "GET":
                    handleGetRatings(exchange, mediaId);
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid media ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void handleRateMedia(HttpExchange exchange, int mediaId, String authHeader) throws IOException {
        var user = authService.getUserByToken(authHeader);
        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        InputStream body = exchange.getRequestBody();
        Map<String, Object> request = mapper.readValue(body, Map.class);

        Integer stars = (Integer) request.get("stars");
        String comment = (String) request.get("comment");

        // Validierung
        if (stars < 1 || stars >5) {
            sendResponse(exchange, 400, "Stars must be between 1 and 5");
            return;
        }

        //Rating erstellen und speichern
        boolean success = ratingManager.rateMedia(mediaId, user.getUsername(), stars, comment);
        if (success) {
            sendResponse(exchange, 201, "Rating created successfully");
        } else {
            sendResponse(exchange, 400, "Could not create rating");
        }
    }

    private void handleGetRatings(HttpExchange exchange, int mediaId) throws IOException {
        var ratings = ratingManager.getRatingsForMedia(mediaId);
        String response = mapper.writeValueAsString(ratings);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        try {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException e) {
            System.err.println("Failed to send response: " + e.getMessage());
            throw e;
        }
    }

    private void handleDeleteRating(HttpExchange exchange, int ratingId, String authHeader) throws IOException {
        var user = authService.getUserByToken(authHeader);
        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        boolean success = ratingManager.deleteRating(ratingId, user.getUsername());

        if (success) {
            sendResponse(exchange, 200, "Rating deleted successfully");
        } else {
            sendResponse(exchange, 404, "Rating not found or not authorized");
        }
    }
}
