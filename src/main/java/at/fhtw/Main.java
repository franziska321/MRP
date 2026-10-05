package at.fhtw;

import at.fhtw.persistence.DatabaseConfig;
import at.fhtw.persistence.UserRepository;
import at.fhtw.server.HttpServerApp;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try {
            try (Connection ignored = DatabaseConfig.getConnection()) {
                System.out.println("Database connected successfully");
            }

            UserRepository userRepository = new UserRepository();
            userRepository.initDatabase();
            System.out.println("Database tables initialized");

            HttpServerApp.startServer();
        } catch (Exception e) {
            System.err.println("Failed to start the server: " + e.getMessage());
        }
    }
}
