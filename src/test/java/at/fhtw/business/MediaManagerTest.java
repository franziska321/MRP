package at.fhtw.business;

import at.fhtw.models.MediaContent;
import at.fhtw.models.Movie;
import at.fhtw.models.User;
import at.fhtw.persistence.MediaRepository;
import at.fhtw.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaManagerTest {
    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    private MediaManager mediaManager;

    @BeforeEach
    void setUp() {
        mediaManager = new MediaManager(mediaRepository, userRepository);
    }

    @Test
    void createMediaWithValidMovieSavesIt() {
        MediaContent movie = movie("Inception", 2010, 7);
        when(mediaRepository.saveMedia(movie)).thenReturn(true);

        assertTrue(mediaManager.createMedia(movie));

        verify(mediaRepository).saveMedia(movie);
    }

    @Test
    void createMediaRejectsMissingTitle() {
        MediaContent movie = movie(" ", 2010, 7);

        assertFalse(mediaManager.createMedia(movie));

        verify(mediaRepository, never()).saveMedia(movie);
    }

    @Test
    void createMediaRejectsInvalidYear() {
        MediaContent movie = movie("Old movie", 1800, 7);

        assertFalse(mediaManager.createMedia(movie));

        verify(mediaRepository, never()).saveMedia(movie);
    }

    @Test
    void createMediaRejectsInvalidType() {
        MediaContent movie = movie("Unknown", 2024, 7);
        movie.setMediaType("podcast");

        assertFalse(mediaManager.createMedia(movie));

        verify(mediaRepository, never()).saveMedia(movie);
    }

    @Test
    void deleteMediaAllowsItsCreator() {
        MediaContent movie = movie("Owned", 2024, 7);
        User creator = new User("creator", "hash");
        creator.setId(7);
        when(mediaRepository.getMediaById(12)).thenReturn(movie);
        when(userRepository.findByUsername("creator")).thenReturn(creator);
        when(mediaRepository.deleteMedia(12)).thenReturn(true);

        assertTrue(mediaManager.deleteMedia(12, "creator"));

        verify(mediaRepository).deleteMedia(12);
    }

    @Test
    void deleteMediaRejectsAnotherUser() {
        MediaContent movie = movie("Owned", 2024, 7);
        User otherUser = new User("other", "hash");
        otherUser.setId(8);
        when(mediaRepository.getMediaById(12)).thenReturn(movie);
        when(userRepository.findByUsername("other")).thenReturn(otherUser);

        assertFalse(mediaManager.deleteMedia(12, "other"));

        verify(mediaRepository, never()).deleteMedia(12);
    }

    private Movie movie(String title, int releaseYear, int userId) {
        Movie movie = new Movie(title, releaseYear, "Description");
        movie.setUserId(userId);
        return movie;
    }
}
