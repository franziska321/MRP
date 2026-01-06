package at.fhtw.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Rating {
    private int id;
    private int mediaId;
    private String username;
    private int stars;
    private String comment;
    private boolean isApproved = false;
    private int likes = 0;

    @JsonIgnore
    private LocalDateTime createdAt;

    private boolean wahr = true;
}
