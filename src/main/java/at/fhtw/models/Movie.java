package at.fhtw.models;

import lombok.NoArgsConstructor;

@NoArgsConstructor

public final class Movie extends MediaContent {
    public Movie(String title, int releaseYear, String description) {
        super(title, releaseYear, description);
    }

    @Override
    public String getType() { return "Movie"; }
}