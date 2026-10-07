package at.fhtw.business;

import at.fhtw.models.User;
import at.fhtw.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {
    @Mock
    private UserRepository userRepository;

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager(userRepository);
    }

    @Test
    void registerUserHashesPasswordAndCreatesRandomToken() {
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(userRepository.saveUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(true);

        assertTrue(userManager.registerUser("alice", "correct horse battery staple"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getPasswordHash().startsWith("pbkdf2$"));
        assertNotEquals("correct horse battery staple", savedUser.getPasswordHash());
        assertDoesNotThrow(() -> UUID.fromString(savedUser.getToken()));
    }

    @Test
    void registerUserRejectsDuplicateUsername() {
        when(userRepository.findByUsername("alice")).thenReturn(new User("alice", "hash"));

        assertFalse(userManager.registerUser("alice", "password"));

        verify(userRepository, never()).saveUser(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerUserRejectsBlankInput() {
        assertFalse(userManager.registerUser(" ", "password"));
        assertFalse(userManager.registerUser("alice", " "));

        verify(userRepository, never()).saveUser(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loginReturnsTokenForValidPassword() {
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(userRepository.saveUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(true);
        assertTrue(userManager.registerUser("alice", "correct password"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        when(userRepository.findByUsername("alice")).thenReturn(savedUser);

        String token = userManager.loginUser("alice", "correct password");

        assertNotNull(token);
        assertDoesNotThrow(() -> UUID.fromString(token));
    }

    @Test
    void loginRejectsInvalidPassword() {
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(userRepository.saveUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(true);
        assertTrue(userManager.registerUser("alice", "correct password"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());
        when(userRepository.findByUsername("alice")).thenReturn(userCaptor.getValue());

        assertNull(userManager.loginUser("alice", "wrong password"));
    }
}
