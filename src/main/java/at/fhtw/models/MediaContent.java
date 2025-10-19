package at.fhtw.models;

import at.fhtw.models.enums.MediaType;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@ToString

public abstract sealed class MediaContent permits Movie, Game, Series{
    private String title;
    private String description;
    private MediaType type;
    private int releaseYear;
    private List<String> genres;
    private int ageRestriction;


    public MediaContent(String title, int releaseYear, String description) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.description = description;
    }

    //Getter
    public String getTitle() { return title; }
    public int getReleaseYear() { return releaseYear; }
    public String getDescription() { return description; }

    // Setter
    public void setTitle(String title) { this.title = title; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public void setDescription(String description) { this.description = description; }


    // für Unterklassen Gettr
    public abstract String getType();

    // Liskov im Prinzip aber das geht besser...
    void rateMedia(int rating){
        System.out.println("Media rated....");
    }


}

