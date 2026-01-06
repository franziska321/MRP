package at.fhtw.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoritesRepository {

    public boolean addFavorite(int userId, int mediaId) {
        String sql = "INSERT INTO favorites (user_id, media_id) VALUES (?, ?) " +
                "ON CONFLICT (user_id, media_id) DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFavorite(int userId, int mediaId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error removing favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean isFavorite(int userId, int mediaId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking favorite: " + e.getMessage());
            return false;
        }
    }

    public List<Integer> getUserFavorites(int userId) {
        List<Integer> favorites = new ArrayList<>();
        String sql = "SELECT media_id FROM favorites WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                favorites.add(rs.getInt("media_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting favorites: " + e.getMessage());
        }
        return favorites;
    }
}
