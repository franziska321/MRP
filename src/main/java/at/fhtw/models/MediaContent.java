package at.fhtw.models;

import at.fhtw.models.enums.MediaType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "mediaType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Movie.class, name = "movie"),
        @JsonSubTypes.Type(value = Game.class, name = "game"),
        @JsonSubTypes.Type(value = Series.class, name = "series")
})

@NoArgsConstructor
@ToString
@Data

public abstract sealed class MediaContent permits Movie, Game, Series{
    private int id;
    private String title;
    private String description;
    private String mediaType; // "movie", "series", "game"
    private int releaseYear;
    private List<String> genres;
    private int ageRestriction;
    private int userId; // Wer hat es erstellt


    public MediaContent(String title, int releaseYear, String description) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.description = description;
    }




    // für Unterklassen Gettr
    public abstract String getType();

    // Liskov im Prinzip aber das geht besser...
    void rateMedia(int rating){
        System.out.println("Media rated....");
    }


}

