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
        var mediaList = mediaManager.getAllMedia();
        String response = mapper.writeValueAsString(mediaList);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, response);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}