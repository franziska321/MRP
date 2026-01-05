package at.fhtw.business;

import at.fhtw.models.Rating;
import at.fhtw.persistence.RatingRepository;

import java.time.LocalDateTime;
import java.util.List;

public class RatingManager {
    private final RatingRepository ratingRepository = new RatingRepository();


    public boolean rateMedia(int mediaID, String username, int stars, String comment) {
        // validate + save ratings
        if (stars < 1 || stars > 5) {
            System.err.println("Invalid rating: " + stars + " (must be 1-5)");
            return false;
        }
        if (hasUserRatedMedia(mediaID, username)) {
            System.err.println("User " + username + " already rated media " + mediaID);
            return false;
        }


        Rating rating = new Rating();
        rating.setMediaId(mediaID);
        rating.setUsername(username);
        rating.setStars(stars);
        rating.setComment(comment);
        rating.setCreatedAt(LocalDateTime.now());

        return ratingRepository.saveRating(rating); // falsch
    }

    public List<Rating> getRatingsForMedia(int mediaId) {
        return ratingRepository.getRatingsForMedia(mediaId);
    }


    private boolean hasUserRatedMedia(int mediaId, String username) {
        return ratingRepository.hasUserRatedMedia(mediaId, username);
    }

    public boolean deleteRating(int ratingId, String username) {
        // Prüfen ob der User dieses Rating erstellt hat

        Rating rating = ratingRepository.getRatingById(ratingId);
        if (rating == null) {
            return false; // Rating existiert nicht
        }

        // Nur der Ersteller kann löschen
        if (!rating.getUsername().equals(username)) {
            return false; // Nicht autorisiert
        }

        return ratingRepository.deleteRating(ratingId);
    }
}
