package at.fhtw.models;

public final class Game extends MediaContent {
    public Game(){
        this.setMediaType("game");
    }

    public Game(String title, int releaseYear, String description) {

        super(title, releaseYear, description);
        this.setMediaType("game");
    }

    @Override
    public String getType() { return "game"; }
}