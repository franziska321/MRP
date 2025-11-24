package at.fhtw;

import at.fhtw.persistence.DatabaseConfig;
import at.fhtw.persistence.UserRepository;
import at.fhtw.presentation.MediaPresentation;
import at.fhtw.server.HttpServerApp;

public class Main {
    public static void main(String[] args) {
    try {
        DatabaseConfig.getConnection();

        System.out.println("Database connected successfully");

        // Tabellen erstellen
        UserRepository userRepository = new UserRepository();
        userRepository.initDatabase();
        System.out.println("Database tables initialized");

        HttpServerApp.startServer();
    } catch (Exception e) {
        System.err.println("Fehler beim Starten des Servers:" + e.getMessage());
    }



    }
}
