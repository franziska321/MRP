package at.fhtw.services;

import at.fhtw.business.UserManager;


public class UserService {
    private final UserManager userManager = new UserManager();

    // Registrierung
    public boolean registerUser(String username, String password) {
        return userManager.registerUser(username, password);
    }

    // Login
    public String login(String username, String password) {
        return userManager.loginUser(username, password);
    }
}

