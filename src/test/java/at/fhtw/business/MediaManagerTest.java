package at.fhtw.business;

import at.fhtw.models.MediaContent;
import at.fhtw.models.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaManagerTest {
    private MediaManager mediaManager;

    @BeforeEach
    void setUp() {
        mediaManager = new MediaManager();
    }

    @Test
    void testCreateMedia_ValidMovie() {
        // Setup
        MediaContent movie = new Movie("Inception", 2010, "Leo DiCaprio wirft seine Frau aus dem Fenster");
        movie.setMediaType("movie");
        movie.setUserId(1);

        // Test & Assert
        assertTrue(mediaManager.createMedia(movie), "Valid movie should be created");
    }

    @Test
    void testCreateMedia_InvalidTitle() {
        // Setup
        MediaContent movie = new Movie("", 2010, "Description"); // Empty title
        movie.setMediaType("movie");


        // Test & Assert
        assertFalse(mediaManager.createMedia(movie), "Empty title should fail");
    }

    @Test
    void testCreateMedia_InvalidYear() {
        // Setup
        MediaContent movie = new Movie("Test", 1800, "Description"); // Invalid year
        movie.setMediaType("movie");

        // Test & Assert
        assertFalse(mediaManager.createMedia(movie), "Invalid year should fail");
    }
}