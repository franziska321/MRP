package at.fhtw.persistence;

import at.fhtw.models.MediaContent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaRepository {

    public boolean saveMedia(MediaContent media) {

        String sql = "INSERT INTO media (title, description, media_type, release_year, genres, age_restriction, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {


            pstmt.setString(1, media.getTitle());
            pstmt.setString(2, media.getDescription());
            pstmt.setString(3, media.getMediaType());
            pstmt.setInt(4, media.getReleaseYear());


            Array genresArray;
            if (media.getGenres() != null && !media.getGenres().isEmpty()) {
                genresArray = conn.createArrayOf("VARCHAR", media.getGenres().toArray());
            } else {
                // Leeres Array falls null
                genresArray = conn.createArrayOf("VARCHAR", new String[0]);
            }
            pstmt.setArray(5, genresArray);

            pstmt.setInt(6, media.getAgeRestriction());
            pstmt.setInt(7, media.getUserId());


            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<MediaContent> getAllMedia() {
        List<MediaContent> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Media Objekt aus ResultSet erstellen
                // (musst ich noch implementieren)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mediaList;
    }
}