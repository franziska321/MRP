package at.fhtw.business;

import at.fhtw.models.Rating;
import at.fhtw.persistence.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingManagerTest {
    @Mock
    private RatingRepository ratingRepository;

    private RatingManager ratingManager;

    @BeforeEach
    void setUp() {
        ratingManager = new RatingManager(ratingRepository);
    }

    @Test
    void rateMediaSavesValidRating() {
        when(ratingRepository.hasUserRatedMedia(5, "alice")).thenReturn(false);
        when(ratingRepository.saveRating(org.mockito.ArgumentMatchers.any(Rating.class))).thenReturn(true);

        assertTrue(ratingManager.rateMedia(5, "alice", 4, "Good"));

        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).saveRating(ratingCaptor.capture());
        Rating savedRating = ratingCaptor.getValue();
        assertEquals(5, savedRating.getMediaId());
        assertEquals("alice", savedRating.getUsername());
        assertEquals(4, savedRating.getStars());
        assertEquals("Good", savedRating.getComment());
        assertNotNull(savedRating.getCreatedAt());
    }

    @Test
    void rateMediaRejectsStarsOutsideRange() {
        assertFalse(ratingManager.rateMedia(5, "alice", 0, "Too low"));
        assertFalse(ratingManager.rateMedia(5, "alice", 6, "Too high"));

        verify(ratingRepository, never()).saveRating(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rateMediaRejectsDuplicateRating() {
        when(ratingRepository.hasUserRatedMedia(5, "alice")).thenReturn(true);

        assertFalse(ratingManager.rateMedia(5, "alice", 4, "Again"));

        verify(ratingRepository, never()).saveRating(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteRatingAllowsItsAuthor() {
        Rating rating = rating(9, "alice");
        when(ratingRepository.getRatingById(9)).thenReturn(rating);
        when(ratingRepository.deleteRating(9)).thenReturn(true);

        assertTrue(ratingManager.deleteRating(9, "alice"));
    }

    @Test
    void deleteRatingRejectsAnotherUser() {
        Rating rating = rating(9, "alice");
        when(ratingRepository.getRatingById(9)).thenReturn(rating);

        assertFalse(ratingManager.deleteRating(9, "bob"));

        verify(ratingRepository, never()).deleteRating(9);
    }

    private Rating rating(int id, String username) {
        Rating rating = new Rating();
        rating.setId(id);
        rating.setUsername(username);
        return rating;
    }
}
