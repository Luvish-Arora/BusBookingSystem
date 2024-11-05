package com.example.busticketbooking.services;

import com.example.busticketbooking.models.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Validates login credentials and returns the username if valid
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
            System.err.println("SQL Exception during login validation: " + e.getMessage());
        }
        return null;
    }

    // Saves the booking details into both a bus-specific table and a general bookings table
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
        String insertBookingQuery = "INSERT INTO bookings (name, bus_number, date, seat_number, total_payment, user_name, email, mobile) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement createTableStmt = connection.prepareStatement(createTableQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
             PreparedStatement insertBookingStmt = connection.prepareStatement(insertBookingQuery)) {

            createTableStmt.executeUpdate();

            insertStmt.setString(1, booking.getName());
            insertStmt.setString(2, booking.getDate());
            insertStmt.setString(3, booking.getSeatNumber());
            insertStmt.setDouble(4, booking.getTotalPayment());
            insertStmt.executeUpdate();

            insertBookingStmt.setString(1, booking.getName());
            insertBookingStmt.setString(2, booking.getBusNumber());
            insertBookingStmt.setString(3, booking.getDate());
            insertBookingStmt.setString(4, booking.getSeatNumber());
            insertBookingStmt.setDouble(5, booking.getTotalPayment());
            insertBookingStmt.setString(6, booking.getUserName());
            insertBookingStmt.setString(7, booking.getEmail());
            insertBookingStmt.setString(8, booking.getMobile());
            insertBookingStmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("SQL Exception during booking save: " + e.getMessage());
            return false;
        }
    }

    // Retrieves a list of future bookings for a specific user
    public List<Booking> getFutureBookings(String username, LocalDate currentDate) {
        String query = "SELECT * FROM bookings WHERE user_name = ? AND date > ?";
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, currentDate.toString());

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    Booking booking = new Booking();
                    booking.setId(resultSet.getLong("id"));
                    booking.setName(resultSet.getString("name"));
                    booking.setBusNumber(resultSet.getString("bus_number"));
                    booking.setDate(resultSet.getString("date"));
                    booking.setSeatNumber(resultSet.getString("seat_number"));
                    booking.setTotalPayment(resultSet.getDouble("total_payment"));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception during retrieving bookings: " + e.getMessage());
        }
        return bookings;
    }

    // Deletes a booking from both the general bookings table and the specific bus table
    public boolean deleteBooking(Long bookingId, String busNumber) {
        String deleteBookingQuery = "DELETE FROM bookings WHERE id = ?";
        String deleteFromBusTableQuery = "DELETE FROM " + busNumber.toLowerCase() + " WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement deleteBookingStmt = connection.prepareStatement(deleteBookingQuery);
             PreparedStatement deleteFromBusTableStmt = connection.prepareStatement(deleteFromBusTableQuery)) {

            deleteBookingStmt.setLong(1, bookingId);
            deleteBookingStmt.executeUpdate();

            deleteFromBusTableStmt.setLong(1, bookingId);
            deleteFromBusTableStmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("SQL Exception during booking deletion: " + e.getMessage());
            return false;
        }
    }
    public List<String> getBookedSeats(String busNumber, String date) {
        System.out.println("Bus Number: " + busNumber);
        System.out.println("Date: " + date);
        String query = "SELECT seat_number FROM " + busNumber.toLowerCase() + " WHERE date = ?";
        List<String> bookedSeats = new ArrayList<>();
    
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {
    
            stmt.setString(1, date);
    
            try (ResultSet resultSet = stmt.executeQuery()) {
                StringBuilder seatsBuilder = new StringBuilder();
                while (resultSet.next()) {
                    seatsBuilder.append(resultSet.getString("seat_number")).append(",");
                }
                // Remove the trailing comma if there were results
                if (seatsBuilder.length() > 0) {
                    seatsBuilder.setLength(seatsBuilder.length() - 1);
                }
    
                // Convert to a single string, remove "(w)", and split into individual seat numbers
                String allSeats = seatsBuilder.toString().replace("(w)", "");
                String[] seatsArray = allSeats.split(",");
    
                // Add seats to the list
                for (String seat : seatsArray) {
                    bookedSeats.add(seat.trim());
                }
            }
    
        } catch (SQLException e) {
            System.err.println("SQL Exception during fetching booked seats: " + e.getMessage());
        }
        return bookedSeats;
    }
    
}   