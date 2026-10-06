package at.fhtw.business;

import at.fhtw.models.User;
import at.fhtw.persistence.UserRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class UserManager {
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PASSWORD_ITERATIONS = 210_000;
    private static final int PASSWORD_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;

    public UserManager() {
        this(new UserRepository());
    }

    UserManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return false; // User existiert bereits
        }

        String hash = hashPassword(password);
        User user = new User(username, hash);

        return userRepository.saveUser(user);
    }

    public String loginUser(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = userRepository.findByUsername(username);

        if (user != null && verifyPassword(password, user.getPasswordHash())) {
            return user.getToken();
        }
        return null;
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        try {
            byte[] hash = derivePasswordKey(password, salt, PASSWORD_ITERATIONS);
            return "pbkdf2$" + PASSWORD_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = derivePasswordKey(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derivePasswordKey(String password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                PASSWORD_KEY_LENGTH
        );
        try {
            return SecretKeyFactory.getInstance(PASSWORD_ALGORITHM)
                    .generateSecret(keySpec)
                    .getEncoded();
        } finally {
            keySpec.clearPassword();
        }
    }
}
