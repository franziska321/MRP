package at.fhtw.business;

import at.fhtw.models.MediaContent;
import at.fhtw.persistence.MediaRepository;

import java.util.List;

public class MediaManager {
    private MediaRepository mediaRepository = new MediaRepository();

    public boolean createMedia(MediaContent media) {
        System.out.println("MediaManager.createMedia called");

        // VALIDIERUNG
        if (media.getTitle() == null || media.getTitle().trim().isEmpty()) {
            System.err.println("Validation failed: Title is required");
            return false;
        }

        if (media.getReleaseYear() < 1900 || media.getReleaseYear() > 2030) {
            System.err.println("Validation failed: Invalid release year");
            return false;
        }

        String type = media.getMediaType() != null ? media.getMediaType() : media.getType();
        if (type == null || (!type.equals("movie") && !type.equals("game") && !type.equals("series"))) {
            System.err.println("Validation failed: Invalid media type: " + type);
            return false;
        }

        System.out.println("Validation passed for: " + media.getTitle());

        try {
            boolean result = mediaRepository.saveMedia(media);
            System.out.println("Save result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error in MediaManager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<MediaContent> getAllMedia() {
        return mediaRepository.getAllMedia();
    }
}