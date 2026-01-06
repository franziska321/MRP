package at.fhtw.server.handlers;

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
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void handleCreateMedia(HttpExchange exchange) throws IOException {

        InputStream body = exchange.getRequestBody();
        MediaContent media = mapper.readValue(body, MediaContent.class);
        boolean success = mediaManager.createMedia(media);

        if (success) {

            sendResponse(exchange, 201, "Media created successfully");
        } else {
            sendResponse(exchange, 400, "Invalid media data");
        }
    }

    private void handleGetMedia(HttpExchange exchange) throws IOException {
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