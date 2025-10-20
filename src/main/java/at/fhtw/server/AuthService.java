package at.fhtw.server;

import at.fhtw.models.User;
import at.fhtw.persistence.UserRepository;

import java.util.List;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    public boolean isAuthorized(String authHeader) {
        return getUserByToken(authHeader) != null;
    }

    public User getUserByToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring("Bearer ".length());

        List<User> users = userRepository.loadUsers();
        for (User u : users) {
            if (u.getToken().equals(token)) {
                return u; // User gefunden
            }
        }

        return null; // Kein User zu Token gefunden
    }
}
