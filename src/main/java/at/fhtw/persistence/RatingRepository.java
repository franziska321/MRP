package at.fhtw.persistence;

import at.fhtw.models.Rating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RatingRepository {
    public boolean saveRating(Rating rating) {
        //SQL für in db
        String sql = "INSERT INTO ratings (media_id, username, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rating.getMediaId());
            pstmt.setString(2, rating.getUsername()); // ← Username statt user_id
            pstmt.setInt(3, rating.getStars());
            pstmt.setString(4, rating.getComment());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRating(int ratingId) {
        String sql = "DELETE FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ratingId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting rating: " + e.getMessage());
            return false;
        }
    }

    public List<Rating> getRatingsForMedia(int mediaId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE media_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setMediaId(rs.getInt("media_id"));
                rating.setUsername(rs.getString("username"));
                rating.setStars(rs.getInt("rating"));
                rating.setComment(rs.getString("comment"));
                rating.setApproved(rs.getBoolean("is_approved"));
                rating.setLikes(rs.getInt("likes"));
                rating.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                ratings.add(rating);
            }

        } catch (SQLException e) {
            System.err.println("Error getting ratings: " + e.getMessage());
        }
        return ratings;
    }

    public boolean hasUserRatedMedia(int mediaId, String username) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE media_id = ? AND username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking if user rated: " + e.getMessage());
        }
        return false;
    }


    public Rating getRatingById(int ratingId) {
        String sql = "SELECT * FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ratingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setMediaId(rs.getInt("media_id"));
                rating.setUsername(rs.getString("username"));
                rating.setStars(rs.getInt("rating"));
                rating.setComment(rs.getString("comment"));
                rating.setApproved(rs.getBoolean("is_approved"));
                rating.setLikes(rs.getInt("likes"));
                rating.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return rating;
            }
        } catch (SQLException e) {
            System.err.println("Error getting rating: " + e.getMessage());
        }
        return null;
    }
}
