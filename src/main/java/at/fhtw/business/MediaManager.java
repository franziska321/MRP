package at.fhtw.business;

import at.fhtw.models.MediaContent;
import at.fhtw.models.User;
import at.fhtw.persistence.MediaRepository;
import at.fhtw.persistence.UserRepository;

import java.util.List;

public class MediaManager {
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public MediaManager() {
        this(new MediaRepository(), new UserRepository());
    }

    MediaManager(MediaRepository mediaRepository, UserRepository userRepository) {
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    public boolean createMedia(MediaContent media) {

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


        try {
            boolean result = mediaRepository.saveMedia(media);
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


    public List<MediaContent> searchAndFilter(String title, String genre, String mediaType,
                                              Integer year, Integer ageRestriction,
                                              Integer minRating, String sortBy) {
        return mediaRepository.searchAndFilter(
                title, genre, mediaType, year, ageRestriction, minRating, sortBy
        );
    }

    public MediaContent getMediaById(int mediaId) {
        return mediaRepository.getMediaById(mediaId);
    }

    public boolean deleteMedia(int mediaId, String requestingUsername) {
        MediaContent media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            return false;
        }

        User user = userRepository.findByUsername(requestingUsername);
        if (user == null) {
            System.err.println("User " + requestingUsername + " not found");
            return false;
        }

        if (media.getUserId() == null) {
            System.err.println("Media has no creator ID");
            return false;
        }

        if (!media.getUserId().equals(user.getId())) {  // ← ID VERGLEICH
            System.err.println("User " + user.getId() + " is not creator " + media.getUserId());
            return false;
        }

        // 4. Löschen
        return mediaRepository.deleteMedia(mediaId);
    }

}

