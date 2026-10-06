package at.fhtw.server;

import at.fhtw.models.User;
import at.fhtw.persistence.UserRepository;

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
        return userRepository.findByToken(token);
    }
}
