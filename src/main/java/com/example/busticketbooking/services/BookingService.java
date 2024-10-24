package com.example.busticketbooking.services;

import com.example.busticketbooking.models.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class BookingService {

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

    // Method to save the booking details into the database
    public void saveBooking(Booking booking) {
        if (booking.getBusNumber() == null || booking.getBusNumber().isEmpty()) {
            throw new IllegalArgumentException("Bus number cannot be null or empty.");
        }

        String tableName = booking.getBusNumber().toLowerCase();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                                  "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                  "name VARCHAR(255), " +
                                  "date VARCHAR(255), " +
                                  "seat_number VARCHAR(10), " +
                                  "total_payment DOUBLE)";
        String insertQuery = "INSERT INTO " + tableName + " (name, date, seat_number, total_payment) VALUES (?, ?, ?, ?)";

        // Insert query for the general bookings table
        String insertQuery1 = "INSERT INTO bookings(name, bus_number, date, seat_number, total_payment) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement createTableStmt = connection.prepareStatement(createTableQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
             PreparedStatement insertStmt1 = connection.prepareStatement(insertQuery1)) {

            // Create table if not exists
            createTableStmt.executeUpdate();

            // Insert booking into the bus-specific table
            insertStmt.setString(1, booking.getName());
            insertStmt.setString(2, booking.getDate());
            insertStmt.setString(3, booking.getSeatNumber());
            insertStmt.setDouble(4, booking.getTotalPayment());
            insertStmt.executeUpdate();

            // Insert booking into the general bookings table
            insertStmt1.setString(1, booking.getName());
            insertStmt1.setString(2, booking.getBusNumber());
            insertStmt1.setString(3, booking.getDate());
            insertStmt1.setString(4, booking.getSeatNumber());
            insertStmt1.setDouble(5, booking.getTotalPayment());
            insertStmt1.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
    }
}
