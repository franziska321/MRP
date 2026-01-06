package at.fhtw.server.handlers;

import at.fhtw.business.RatingManager;
import at.fhtw.models.User;
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


        // Auth check für ALLE Endpoints außer OPTIONS
        if (!"OPTIONS".equals(method)) {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (!authService.isAuthorized(authHeader)) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }
        }

        try {
            // Nur eine Route: /api/media/rate
            if (!"/api/media/rate".equals(path)) {
                sendResponse(exchange, 404, "Not found");
                return;
            }

            // Je nach Methode unterschiedliche Handler
            switch(method) {
                case "POST":
                    handleCreateRating(exchange);
                    break;
                case "GET":
                    handleGetRatings(exchange);
                    break;
                case "DELETE":
                    handleDeleteRating(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleCreateRating(HttpExchange exchange) throws IOException {

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        User user = authService.getUserByToken(authHeader);

        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        InputStream body = exchange.getRequestBody();
        Map<String, Object> request = mapper.readValue(body, Map.class);

        // mediaId aus JSON Body lesen
        Integer mediaId = (Integer) request.get("mediaId");
        Integer stars = (Integer) request.get("stars");
        String comment = (String) request.get("comment");


        // Validierung
        if (mediaId == null || mediaId <= 0) {
            sendResponse(exchange, 400, "Invalid media ID");
            return;
        }

        if (stars == null || stars < 1 || stars > 5) {
            sendResponse(exchange, 400, "Stars must be between 1 and 5");
            return;
        }

        // Rating erstellen und speichern
        boolean success = ratingManager.rateMedia(mediaId, user.getUsername(), stars, comment);

        if (success) {
            sendResponse(exchange, 201, "Rating created successfully");
        } else {
            sendResponse(exchange, 400, "Could not create rating (already rated?)");
        }
    }

    private void handleGetRatings(HttpExchange exchange) throws IOException {

        // mediaId aus Query-Parameter lesen
        String query = exchange.getRequestURI().getQuery();
        Integer mediaId = null;

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("mediaId=")) {
                    try {
                        mediaId = Integer.parseInt(param.substring(8));
                    } catch (NumberFormatException e) {
                        sendResponse(exchange, 400, "Invalid media ID format");
                        return;
                    }
                }
            }
        }

        if (mediaId == null) {
            sendResponse(exchange, 400, "Query parameter 'mediaId' is required");
            return;
        }

        System.err.println("Getting ratings for media: " + mediaId);

        var ratings = ratingManager.getRatingsForMedia(mediaId);
        String response = mapper.writeValueAsString(ratings);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void handleDeleteRating(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        User user = authService.getUserByToken(authHeader);

        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        // ratingId aus Query-Parameter lesen
        String query = exchange.getRequestURI().getQuery();
        Integer ratingId = null;

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("ratingId=")) {
                    try {
                        ratingId = Integer.parseInt(param.substring(9));
                    } catch (NumberFormatException e) {
                        sendResponse(exchange, 400, "Invalid rating ID format");
                        return;
                    }
                }
            }
        }

        if (ratingId == null) {
            sendResponse(exchange, 400, "Query parameter 'ratingId' is required");
            return;
        }


        boolean success = ratingManager.deleteRating(ratingId, user.getUsername());

        if (success) {
            sendResponse(exchange, 200, "Rating deleted successfully");
        } else {
            sendResponse(exchange, 403, "Rating not found or not authorized to delete");
        }
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
}
