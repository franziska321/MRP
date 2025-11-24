package at.fhtw.models;

import lombok.NoArgsConstructor;



public final class Movie extends MediaContent {
    public Movie() {
        this.setMediaType("movie");
    }


    public Movie(String title, int releaseYear, String description) {
        super(title, releaseYear, description);
        this.setMediaType("movie");
    }

    @Override
    public String getType() { return "movie"; }
}