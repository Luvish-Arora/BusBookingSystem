package com.example.busticketbooking.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class UserService {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Validates login credentials
    public boolean validateLogin(String username, String password) {
        String query = "SELECT 1 FROM user_login WHERE user_name = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception during login validation: " + e.getMessage());
            return false;
        }
    }

    // Creates a new user during signup
    public boolean createUser(String username, String email, String password, String mobile) {
        if (userExists(username)) {
            System.err.println("User already exists with username: " + username);
            return false;
        }

        String insertQuery = "INSERT INTO user_login (user_name, email, password, mobile) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(insertQuery)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);  // Consider using a hashed password
            stmt.setString(4, mobile);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Exception during user creation: " + e.getMessage());
            return false;
        }
    }

    // Helper method to check if a username already exists
    private boolean userExists(String username) {
        String query = "SELECT 1 FROM user_login WHERE user_name = ?";
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception while checking user existence: " + e.getMessage());
            return false;
        }
    }
}
