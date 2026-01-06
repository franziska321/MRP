package at.fhtw.server.handlers;

import at.fhtw.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import at.fhtw.business.MediaManager;
import at.fhtw.models.MediaContent;
import at.fhtw.server.AuthService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaHandler implements HttpHandler {
    private final MediaManager mediaManager = new MediaManager();
    private final AuthService authService = new AuthService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        // Authorization check
        if (!authService.isAuthorized(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        try {
            switch (method) {
                case "POST":
                    handleCreateMedia(exchange);
                    break;
                case "GET":
                    handleGetMedia(exchange);
                    break;
                case "DELETE":
                    handleDeleteMedia(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void handleCreateMedia(HttpExchange exchange) throws IOException {
        // User new
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        User user = authService.getUserByToken(authHeader);

        if (user == null) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }
        //User end
        InputStream body = exchange.getRequestBody();
        MediaContent media = mapper.readValue(body, MediaContent.class);

        media.setUserId(user.getId());
        boolean success = mediaManager.createMedia(media);

        if (success) {

            sendResponse(exchange, 201, "Media created successfully");
        } else {
            sendResponse(exchange, 400, "Invalid media data");
        }
    }

    private void handleGetMedia(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Path: /api/media/{id}
        String[] pathParts = path.split("/");

        if (pathParts.length == 4) {
            // /api/media/{id} → GET BY ID
            try {
                int mediaId = Integer.parseInt(pathParts[3]);
                handleGetMediaById(exchange, mediaId);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid media ID");
            }

        } else if (pathParts.length == 3) {
            // /api/media → SUCHEN/FILTERN
            handleMediaCollection(exchange);
        } else {
            sendResponse(exchange, 404, "Not found");
        }
    }

    private void handleMediaCollection(HttpExchange exchange) throws IOException {
        Map<String, String> params = getQueryParams(exchange);
        String title = params.get("title");
        String genre = params.get("genre");
        String mediaType = params.get("mediaType");
        Integer year = parseInteger(params.get("releaseYear"));
        Integer ageRestriction = parseInteger(params.get("ageRestriction"));
        Integer minRating = parseInteger(params.get("minRating"));
        String sortBy = params.get("sortBy"); // "title", "year", "score"

        var mediaList = mediaManager.searchAndFilter(
                title, genre, mediaType, year, ageRestriction, minRating, sortBy
        );


        String response = mapper.writeValueAsString(mediaList);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void handleGetMediaById(HttpExchange exchange, int mediaId) throws IOException {
        MediaContent media = mediaManager.getMediaById(mediaId);

        if (media == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }

        String response = mapper.writeValueAsString(media);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void handleDeleteMedia(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 4) { // /api/media/{id}
            sendResponse(exchange, 400, "Invalid URL");
            return;
        }

        try {
            int mediaId = Integer.parseInt(pathParts[3]);
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            User user = authService.getUserByToken(authHeader);

            boolean success = mediaManager.deleteMedia(mediaId, user.getUsername());

            if (success) {
                sendResponse(exchange, 200, "Media deleted successfully");
            } else {
                sendResponse(exchange, 403, "Not authorized to delete or media not found");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid media ID");
        }
    }

    private String getQueryParam(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            if (param.startsWith(paramName + "=")) {
                return param.substring(paramName.length() + 1);
            }
        }
        return null;
    }

    private Map<String, String> getQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer value: " + value);
            return null;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}