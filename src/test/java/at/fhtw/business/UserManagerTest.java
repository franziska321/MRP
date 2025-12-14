package at.fhtw.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void registerUser() {
        //delete
        boolean result = userManager.registerUser("testuser0", "test123");
        assertTrue(result, "Registration should succeed");
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        userManager.registerUser("existinguser", "pass123");

        boolean result = userManager.registerUser("existinguser", "newpass");

        assertFalse(result, "Duplicate username should fail");
    }

    @Test
    void loginUser_valid() {
        userManager.registerUser("loginuser", "test123");

        String token = userManager.loginUser("loginuser", "test123");

        assertNotNull(token, "Valid login should return token");
        assertTrue(token.contains("loginuser"), "Token should contain username");
    }

    @Test
    void testLoginUser_InvalidPassword() {
        userManager.registerUser("user1", "correctpass");
        String token = userManager.loginUser("user1", "wrongpass");

        assertNull(token, "Invalid password should return null");
    }
}