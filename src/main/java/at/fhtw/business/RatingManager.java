package at.fhtw.business;

import at.fhtw.models.Rating;
import at.fhtw.persistence.RatingRepository;

import java.time.LocalDateTime;
import java.util.List;

public class RatingManager {
    private final RatingRepository ratingRepository;

    public RatingManager() {
        this(new RatingRepository());
    }

    RatingManager(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }
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

        return ratingRepository.saveRating(rating);
    }

    public List<Rating> getRatingsForMedia(int mediaId) {
        return ratingRepository.getRatingsForMedia(mediaId);
    }


    private boolean hasUserRatedMedia(int mediaId, String username) {
        return ratingRepository.hasUserRatedMedia(mediaId, username);
    }

    public boolean deleteRating(int ratingId, String username) {
        Rating rating = ratingRepository.getRatingById(ratingId);
        if (rating == null) {
            System.err.println("Rating not found: " + ratingId);
            return false;
        }

        if (!rating.getUsername().equals(username)) {
            System.err.println("User " + username + " not authorized to delete rating " + ratingId);
            return false;
        }

        return ratingRepository.deleteRating(ratingId);
    }

    public List<Rating> getAllRatingsForMedia(int mediaId) {
        return ratingRepository.getAllRatingsForMedia(mediaId);
    }

    public Rating getRatingById(int ratingId) {
        return ratingRepository.getRatingById(ratingId);
    }

    public boolean approveRating(int ratingId) {
        return ratingRepository.approveRating(ratingId);
    }
}
