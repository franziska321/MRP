package at.fhtw.models;

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
    private LocalDateTime createdAt;
}
