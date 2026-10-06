package at.fhtw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {
    private Integer id;
    private  String username;
    private String passwordHash;
    private String token;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.token = UUID.randomUUID().toString();
        this.id = null; //set by DB
    }
}
