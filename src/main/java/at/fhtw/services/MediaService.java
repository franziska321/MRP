package at.fhtw.services;

import at.fhtw.business.MediaManager;

public class MediaService {
    private MediaManager mediaManager = new MediaManager();

    public void addMedia(String type, String title, int year, String description) {
        switch(type.toLowerCase()) {
            case "movie":
                mediaManager.addMovie(title, year, description);
                break;
            case "game":
                mediaManager.addGame(title, year, description);
                break;
            case "series":
                mediaManager.addSeries(title, year, description);
                break;
            default:
                throw new IllegalArgumentException("Unknown media type");
        }
    }
}
