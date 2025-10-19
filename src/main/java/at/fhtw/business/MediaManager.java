package at.fhtw.business;

import at.fhtw.models.Game;
import at.fhtw.models.MediaContent;
import at.fhtw.models.Movie;
import at.fhtw.models.Series;
import at.fhtw.persistence.MediaRepository;

public class MediaManager {
    private MediaRepository mediaRepository = new MediaRepository();

    public void addMedia(MediaContent media) {
        System.out.println("Adding media: " + media.getTitle());
    }
    public void saveMedia(MediaContent media) {
        mediaRepository.save(media);
    }

    public void addMovie(String title, int year, String description) {
        // Regeln für Movie
        Movie movie = new Movie(title, year, description);
        mediaRepository.save(movie);
    }

    public void addGame(String title, int year, String description) {
        Game game = new Game(title, year, description);
        mediaRepository.save(game);
    }

    public void addSeries(String title, int year, String description) {
        Series series = new Series(title, year, description);
        mediaRepository.save(series);
    }
}
