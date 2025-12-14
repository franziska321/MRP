package at.fhtw.persistence;

import at.fhtw.models.Rating;

import java.util.List;

public class RatingRepository {
    public boolean saveRating(Rating rating) {
        //SQL für in db
        return true;
    }

    public List<Rating> getAllRatings() {
        // Select * from ratings where media_id = ?
        return null;
    }
}
