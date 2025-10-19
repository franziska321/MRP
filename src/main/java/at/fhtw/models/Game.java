package at.fhtw.models;

public final class Game extends MediaContent {
    public Game(String title, int releaseYear, String description) {
        super(title, releaseYear, description);
    }

    @Override
    public String getType() { return "Game"; }
}