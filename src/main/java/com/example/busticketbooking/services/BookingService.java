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

    /**
     * Validates login credentials and returns the username if valid.
     * @param username The username to validate.
     * @param password The password to validate.
     * @return The username if credentials are valid; null otherwise.
     */
    public String validateLogin(String username, String password) {
        String query = "SELECT user_name FROM user_login WHERE user_name = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("user_name");
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Exception during login: " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves the booking details into both a bus-specific table and a general bookings table.
     * @param booking The Booking object containing the booking details.
     * @return true if the booking is successfully saved; false otherwise.
     */
    public boolean saveBooking(Booking booking) {
        if (booking.getBusNumber() == null || booking.getBusNumber().isEmpty()) {
            throw new IllegalArgumentException("Bus number cannot be null or empty.");
        }

        if (booking.getUserName() == null || booking.getUserName().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }

        String tableName = booking.getBusNumber().toLowerCase();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                                  "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                  "name VARCHAR(255), " +
                                  "date VARCHAR(255), " +
                                  "seat_number VARCHAR(10), " +
                                  "total_payment DOUBLE)";
        
        String insertQuery = "INSERT INTO " + tableName + " (name, date, seat_number, total_payment) VALUES (?, ?, ?, ?)";
        String insertBookingQuery = "INSERT INTO bookings (name, bus_number, date, seat_number, total_payment, user_name,email, mobile) VALUES (?, ?, ?, ?, ?, ?,?,?)";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement createTableStmt = connection.prepareStatement(createTableQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
             PreparedStatement insertBookingStmt = connection.prepareStatement(insertBookingQuery)) {

            // Create bus-specific table if it does not exist
            createTableStmt.executeUpdate();

            // Insert booking into the bus-specific table
            insertStmt.setString(1, booking.getName());
            insertStmt.setString(2, booking.getDate());
            insertStmt.setString(3, booking.getSeatNumber());
            insertStmt.setDouble(4, booking.getTotalPayment());
            insertStmt.executeUpdate();

            // Insert booking into the general bookings table with user name
            insertBookingStmt.setString(1, booking.getName());
            insertBookingStmt.setString(2, booking.getBusNumber());
            insertBookingStmt.setString(3, booking.getDate());
            insertBookingStmt.setString(4, booking.getSeatNumber());
            insertBookingStmt.setDouble(5, booking.getTotalPayment());
            insertBookingStmt.setString(6, booking.getUserName()); // Set username here
            insertBookingStmt.setString(7, booking.getEmail());
            insertBookingStmt.setString(8, booking.getMobile());
            insertBookingStmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("SQL Exception during booking save: " + e.getMessage());
            return false;
        }
    }
}
