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


    public MediaContent getMediaById(int id) {
        String sql = "SELECT * FROM media WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createMediaFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting media by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteMedia(int id) {
        String sql = "DELETE FROM media WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting media: " + e.getMessage());
            return false;
        }
    }


    public List<MediaContent> searchAndFilter(String title, String genre, String mediaType,
                                              Integer year, Integer ageRestriction,
                                              Integer minRating, String sortBy) {
        StringBuilder sql = new StringBuilder("""
        SELECT m.*, COALESCE(AVG(r.rating), 0) as avg_rating
        FROM media m
        LEFT JOIN ratings r ON m.id = r.media_id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (title != null) {
            sql.append(" AND LOWER(m.title) LIKE LOWER(?)");
            params.add("%" + title + "%");
        }

        if (genre != null) {
            sql.append(" AND ? = ANY(m.genres)");
            params.add(genre);
        }

        if (mediaType != null) {
            sql.append(" AND m.media_type = ?");
            params.add(mediaType.toLowerCase());
        }

        if (year != null) {
            sql.append(" AND m.release_year = ?");
            params.add(year);
        }

        if (ageRestriction != null) {
            sql.append(" AND m.age_restriction <= ?");
            params.add(ageRestriction);
        }

        sql.append(" GROUP BY m.id");

        if (minRating != null) {
            sql.append(" HAVING COALESCE(AVG(r.rating), 0) >= ?");
            params.add(minRating);
        }

        if (sortBy != null) {
            sql.append(" ORDER BY ");
            switch (sortBy.toLowerCase()) {
                case "title":
                    sql.append("m.title ASC");
                    break;
                case "year":
                    sql.append("m.release_year DESC");
                    break;
                case "score":
                    sql.append("avg_rating DESC");
                    break;
                default:
                    sql.append("m.id");
            }
        }

        List<MediaContent> mediaList = new ArrayList<>();
        String finalSql = sql.toString();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(finalSql)) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MediaContent media = createMediaFromResultSet(rs);
                mediaList.add(media);
            }

        } catch (SQLException e) {
            System.err.println("Error in searchAndFilter: " + e.getMessage());
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