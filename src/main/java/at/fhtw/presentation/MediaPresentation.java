package at.fhtw.presentation;

import at.fhtw.services.MediaService;

public class MediaPresentation {
    private MediaService mediaService = new MediaService();

    public void createMedia(String type, String title, int year, String description) {
        // Minimal-Validierung
        if (title == null || title.isEmpty()) throw new IllegalArgumentException("Title required");

        // Weitergabe an Service
        mediaService.addMedia(type, title, year, description);

        System.out.println(type + " added: " + title + " (" + year + ")");
    }
}
