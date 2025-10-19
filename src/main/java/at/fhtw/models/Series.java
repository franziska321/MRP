package at.fhtw.models;

public final class Series extends MediaContent {
    public Series(String title, int releaseYear, String description) {
        super(title, releaseYear, description);
    }

    @Override
    public String getType() { return "Series"; }
}