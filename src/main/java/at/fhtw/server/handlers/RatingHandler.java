package at.fhtw.server.handlers;

import at.fhtw.business.MediaManager;
import at.fhtw.business.RatingManager;
import at.fhtw.models.MediaContent;
import at.fhtw.models.Rating;
import at.fhtw.models.User;
import at.fhtw.server.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class RatingHandler implements HttpHandler {
    private final RatingManager ratingManager = new RatingManager();
    private final MediaManager mediaManager = new MediaManager();
    private final AuthService authService = new AuthService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!authService.isAuthorized(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        try {
            // 1. /api/media/ratings/approve
            if ("/api/media/ratings/approve".equals(path)) {
                if ("POST".equals(method)) {
                    handleApproveRating(exchange, authHeader);
                } else {
                    sendResponse(exchange, 405, "Method not allowed");
                }
                return;
            }

            // 2. /api/media/{id}/ratings/all (für Creator)
            if (path.matches("/api/media/\\d+/ratings/all")) {
                String[] parts = path.split("/");
                int mediaId = Integer.parseInt(parts[3]);

                if ("GET".equals(method)) {
                    handleGetAllRatings(exchange, mediaId, authHeader);
                } else {
                    sendResponse(exchange, 405, "Method not allowed");
                }
                return;
            }

            // 3. /api/media/ratings (normale Rating-Operationen)
            if ("/api/media/ratings".equals(path)) {
                switch(method) {
                    case "POST":
                        handleCreateRating(exchange, authHeader);
                        break;
                    case "GET":
                        handleGetRatings(exchange);
                        break;
                    case "DELETE":
                        handleDeleteRating(exchange, authHeader);
                        break;
                    default:
                        sendResponse(exchange, 405, "Method not allowed");
                }
                return;
            }

            sendResponse(exchange, 404, "Not found");

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleCreateRating(HttpExchange exchange, String authHeader) throws IOException {
        //String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        User user = authService.getUserByToken(authHeader);

        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        InputStream body = exchange.getRequestBody();
        Map<String, Object> request = mapper.readValue(body, Map.class);

        Integer mediaId = (Integer) request.get("mediaId");
        Integer stars = (Integer) request.get("stars");
        String comment = (String) request.get("comment");

        if (mediaId == null || mediaId <= 0) {
            sendResponse(exchange, 400, "Invalid media ID");
            return;
        }

        if (stars == null || stars < 1 || stars > 5) {
            sendResponse(exchange, 400, "Stars must be between 1 and 5");
            return;
        }

        boolean success = ratingManager.rateMedia(mediaId, user.getUsername(), stars, comment);

        if (success) {
            sendResponse(exchange, 201, "Rating created successfully");
        } else {
            sendResponse(exchange, 400, "Could not create rating (already rated?)");
        }
    }

    private void handleGetRatings(HttpExchange exchange) throws IOException {
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

        var ratings = ratingManager.getRatingsForMedia(mediaId);
        String response = mapper.writeValueAsString(ratings);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void handleDeleteRating(HttpExchange exchange, String authHeader) throws IOException {
        //String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        User user = authService.getUserByToken(authHeader);

        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

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

    private void handleGetAllRatings(HttpExchange exchange, int mediaId, String authHeader) throws IOException {
        User user = authService.getUserByToken(authHeader);
        MediaContent media = mediaManager.getMediaById(mediaId);

        if (media == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }

        if (!media.getUserId().equals(user.getId())) {
            sendResponse(exchange, 403, "Only media creator can view all ratings");
            return;
        }

        List<Rating> allRatings = ratingManager.getAllRatingsForMedia(mediaId);
        String response = mapper.writeValueAsString(allRatings);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void handleApproveRating(HttpExchange exchange, String authHeader) throws IOException {
        User user = authService.getUserByToken(authHeader);

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
            sendResponse(exchange, 400, "ratingId parameter required");
            return;
        }

        Rating rating = ratingManager.getRatingById(ratingId);
        if (rating == null) {
            sendResponse(exchange, 404, "Rating not found");
            return;
        }

        MediaContent media = mediaManager.getMediaById(rating.getMediaId());
        if (media == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }

        if (!media.getUserId().equals(user.getId())) {
            sendResponse(exchange, 403, "Only media creator can approve ratings");
            return;
        }

        boolean success = ratingManager.approveRating(ratingId);

        if (success) {
            sendResponse(exchange, 200, "Rating approved successfully");
        } else {
            sendResponse(exchange, 500, "Failed to approve rating");
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