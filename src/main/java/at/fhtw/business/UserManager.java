package at.fhtw.business;

import at.fhtw.models.User;
import at.fhtw.persistence.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class UserManager {
    private final UserRepository userRepository = new UserRepository();

    public boolean registerUser(String username, String password) {
        List<User> users = userRepository.loadUsers();

        // prüfen, ob Username schon existiert
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return false; // already exists
            }
        }

        String hash = hashPassword(password);
        users.add(new User(username, hash));
        userRepository.saveUsers(users);
        return true;
    }

    public String loginUser(String username, String password) {
        List<User> users = userRepository.loadUsers();
        String hash = hashPassword(password);

        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPasswordHash().equals(hash)) {
                return u.getToken();
            }
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
