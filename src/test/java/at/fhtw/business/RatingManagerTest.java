package at.fhtw.business;

import at.fhtw.models.Rating;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RatingManagerTest {

    private static RatingManager ratingManager;
    private static UserManager userManager;
    private static final String TEST_USER = "rating_test_user";
    private static final String TEST_USER_2 = "rating_test_user_2";
    private static final int TEST_MEDIA_ID = 999; // Hohe ID für Test-Medien


    @BeforeAll
    static void setUpOnce() {
        userManager = new UserManager();
        ratingManager = new RatingManager();

        createTestUserIfNotExists(TEST_USER);
        createTestUserIfNotExists(TEST_USER_2);

    }

    @BeforeEach
    void setUp() {
        cleanTestRatings();

    }

    @Test
    void rateMedia_ValidRating_Success() {
        // Act
        boolean result = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 5, "Excellent movie!");

        // Assert
        assertTrue(result, "Valid rating (5 stars) should succeed");
    }

    @Test
    void rateMedia_ValidRatingWith3Stars_Success() {
        // Act
        boolean result = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 3, "Good movie");

        // Assert
        assertTrue(result, "Valid rating (3 stars) should succeed");
    }

    @Test
    void rateMedia_ValidRatingWithNullComment_Success() {
        // Act
        boolean result = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 4, null);

        // Assert
        assertTrue(result, "Rating with null comment should succeed");
    }

    @Test
    void rateMedia_ValidRatingWithEmptyComment_Success() {
        // Act
        boolean result = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 4, "");

        // Assert
        assertTrue(result, "Rating with empty comment should succeed");
    }

    @Test
    void rateMedia_StarsTooLow_Fails() {
        // Act
        boolean result = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 0, "Too low");

        // Assert
        assertFalse(result, "Stars < 1 should fail");
    }

    @Test
    void rateMedia_StarsTooHigh_Fails() {
        // Act
        boolean result = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 6, "Too high");

        // Assert
        assertFalse(result, "Stars > 5 should fail");
    }

    @Test
    void rateMedia_DuplicateRating_Fails() {
        // Arrange - 1st rating
        boolean firstResult = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 4, "First rating");
        assertTrue(firstResult, "First rating should succeed");

        // Act - 2nd rating for same media
        boolean secondResult = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 5, "Second rating");

        // Assert
        assertFalse(secondResult, "Duplicate rating should fail");
    }

    @Test
    void rateMedia_DifferentUsersCanRateSameMedia() {
        // Arrange - User 1 rates
        boolean result1 = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 5, "From user 1");
        assertTrue(result1, "User 1 rating should succeed");

        // Act - User 2 bewertet dasselbe Media
        boolean result2 = ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER_2, 4, "From user 2");

        // Assert
        assertTrue(result2, "Different user should be able to rate same media");
    }

    @Test
    void getRatingsForMedia_NoRatings_ReturnsEmptyList() {
        // Act
        List<Rating> ratings = ratingManager.getRatingsForMedia(TEST_MEDIA_ID);

        // Assert
        assertNotNull(ratings, "Should return list (not null)");
        assertTrue(ratings.isEmpty(), "Should return empty list for media with no ratings");
    }

    @Test
    void getRatingsForMedia_WithOneRating_ReturnsListWithOneItem() {
        // Arrange
        ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 5, "Single rating");

        // Act
        List<Rating> ratings = ratingManager.getRatingsForMedia(TEST_MEDIA_ID);

        // Assert
        assertNotNull(ratings);
        assertEquals(1, ratings.size(), "Should return list with one rating");

        Rating rating = ratings.get(0);
        assertEquals(TEST_MEDIA_ID, rating.getMediaId());
        assertEquals(TEST_USER, rating.getUsername());
        assertEquals(5, rating.getStars());
        assertEquals("Single rating", rating.getComment());
    }

    @Test
    void getRatingsForMedia_WithMultipleRatings_ReturnsCorrectList() {
        // Arrange - Zwei Ratings erstellen
        ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER, 5, "First rating");
        ratingManager.rateMedia(TEST_MEDIA_ID, TEST_USER_2, 3, "Second rating");

        // Act
        List<Rating> ratings = ratingManager.getRatingsForMedia(TEST_MEDIA_ID);

        // Assert
        assertNotNull(ratings);
        assertEquals(2, ratings.size(), "Should return list with two ratings");

        boolean hasUser1 = ratings.stream().anyMatch(r -> TEST_USER.equals(r.getUsername()));
        boolean hasUser2 = ratings.stream().anyMatch(r -> TEST_USER_2.equals(r.getUsername()));

        assertTrue(hasUser1, "Should contain rating from TEST_USER");
        assertTrue(hasUser2, "Should contain rating from TEST_USER_2");
    }

    // ===== HELPER METHODS =====

    private static void createTestUserIfNotExists(String username) {
        try {
            String token = userManager.loginUser(username, "test123");
            if (token == null) {
                boolean created = userManager.registerUser(username, "test123");
                if (created) {
                    System.out.println("Created test user: " + username);
                }
            }
        } catch (Exception e) {
            userManager.registerUser(username, "test123");
        }


    }

    private void cleanTestRatings() {
        try {
            // Verbindung zur DB und Ratings löschen
            java.sql.Connection conn = at.fhtw.persistence.DatabaseConfig.getConnection();
            java.sql.Statement stmt = conn.createStatement();

            // Ratings der Test-User für Test-Media löschen
            String sql = "DELETE FROM ratings WHERE username IN ('" + TEST_USER + "', '" + TEST_USER_2 + "')";
            stmt.executeUpdate(sql);

            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }


}