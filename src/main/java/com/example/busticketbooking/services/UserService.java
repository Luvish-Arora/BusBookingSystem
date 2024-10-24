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

    // Method to validate login credentials
    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM user_login WHERE user_name = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set the parameters for the SQL query
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Execute the query
            try (ResultSet resultSet = stmt.executeQuery()) {
                // If a record exists, login is successful
                return resultSet.next();
            }

        } catch (SQLException e) {
            // Handle SQL exceptions
            System.err.println("SQL Exception during login: " + e.getMessage());
            return false;
        }
    }
}
