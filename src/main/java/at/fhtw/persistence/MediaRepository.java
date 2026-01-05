package at.fhtw.persistence;

import at.fhtw.models.Game;
import at.fhtw.models.MediaContent;
import at.fhtw.models.Movie;
import at.fhtw.models.Series;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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
                MediaContent media = createMediaFromResultSet(rs);
                mediaList.add(media);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mediaList;
    }

    public List<MediaContent> searchByTitle(String title) {
        List<MediaContent> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE LOWER(title) LIKE LOWER(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + title + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MediaContent media = createMediaFromResultSet(rs);
                mediaList.add(media);
            }

        } catch (SQLException e) {
            System.err.println("Error searching media: " + e.getMessage());
        }
        return mediaList;
    }

    private MediaContent createMediaFromResultSet(ResultSet rs) throws SQLException {
        String mediaType = rs.getString("media_type");
        MediaContent media = switch (mediaType.toLowerCase()) {
            case "movie" -> new Movie(
                    rs.getString("title"),
                    rs.getInt("release_year"),
                    rs.getString("description")
            );
            case "game" -> new Game(
                    rs.getString("title"),
                    rs.getInt("release_year"),
                    rs.getString("description")
            );
            case "series" -> new Series(
                    rs.getString("title"),
                    rs.getInt("release_year"),
                    rs.getString("description")
            );
            default ->
                    new Movie(
                            rs.getString("title"),
                            rs.getInt("release_year"),
                            rs.getString("description")
                    );
        };

        media.setId(rs.getInt("id"));
        media.setMediaType(mediaType);

        // Genres (Array aus PostgreSQL)
        Array genresArray = rs.getArray("genres");
        if (genresArray != null) {
            String[] genres = (String[]) genresArray.getArray();
            media.setGenres(Arrays.asList(genres));
        }

        media.setAgeRestriction(rs.getInt("age_restriction"));
        media.setUserId(rs.getInt("user_id"));

        return media;

    }
}