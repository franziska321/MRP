package at.fhtw.persistence;

import at.fhtw.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {



    public void initDatabase() {

        try (Connection conn = DatabaseConfig.getConnection()) {
            Statement stmt = conn.createStatement();
            // Users Tabelle
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "token VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Media Tabelle
            stmt.execute("CREATE TABLE IF NOT EXISTS media (" +
                    "id SERIAL PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "media_type VARCHAR(20) NOT NULL, " +
                    "release_year INTEGER, " +
                    "genres TEXT[], " +
                    "age_restriction INTEGER, " +
                    "user_id INTEGER REFERENCES users(id), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password_hash")
                );
                user.setId(rs.getInt("id"));
                user.setToken(rs.getString("token"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    public boolean saveUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, token) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getToken());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));  // ← id from db
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password_hash")
                );
                user.setId(rs.getInt("id"));
                user.setToken(rs.getString("token"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }
}