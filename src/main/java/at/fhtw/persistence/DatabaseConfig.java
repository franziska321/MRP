package at.fhtw.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:postgresql://localhost:5433/mrp"
    );
    private static final String USER = System.getenv().getOrDefault("DB_USER", "mrp");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, requiredEnvironmentVariable("DB_PASSWORD"));
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }
}
