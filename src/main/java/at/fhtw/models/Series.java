package at.fhtw.models;

public final class Series extends MediaContent {
    public Series(){
        this.setMediaType("series");
    }
    public Series(String title, int releaseYear, String description) {
        super(title, releaseYear, description);
        this.setMediaType("series");
    }

    @Override
    public String getType() { return "series"; }
}