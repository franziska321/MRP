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
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return false; // User existiert bereits
        }

        String hash = hashPassword(password);
        User user = new User(username, hash);

        return userRepository.saveUser(user);
    }

    public String loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        String hash = hashPassword(password);

        if (user != null && user.getPasswordHash().equals(hash)) {
            return user.getToken();
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
