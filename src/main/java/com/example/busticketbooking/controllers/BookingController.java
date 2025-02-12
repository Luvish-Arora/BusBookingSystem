package com.example.busticketbooking.controllers;

import com.example.busticketbooking.models.Booking;
import com.example.busticketbooking.services.BookingService;
import com.example.busticketbooking.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.example.busticketbooking.models.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
@SessionAttributes({"booking", "username", "action"}) // Store booking, username, and action in session
public class BookingController {

    private String booking;
    private String date;


    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;
    
    private static final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static class LoginAttempt {
        int attempts;
        LocalDateTime lastAttempt;
        
        LoginAttempt() {
            this.attempts = 1;
            this.lastAttempt = LocalDateTime.now();
        }
        
        void increment() {
            this.attempts++;
            this.lastAttempt = LocalDateTime.now();
        }
    }
    
    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "action", required = false) String action) {
        model.addAttribute("action", action);
        return "login";
    }
    
    @PostMapping("/login")
public String processLogin(@RequestParam("identifier") String identifier,
                         @RequestParam("password") String password,
                         @RequestParam(value = "action", required = false) String action,
                         Model model) {
    
    boolean hasError = false;
    
    // Preserve the entered identifier
    model.addAttribute("identifier", identifier);
    model.addAttribute("action", action);
    
    // Validate username
    if (identifier == null || identifier.trim().isEmpty()) {
        model.addAttribute("identifierError", "Please enter your phone number or email");
        hasError = true;
    } else if (!isValidIdentifier(identifier.trim())) {
        model.addAttribute("identifierError", "Please enter a valid phone number or email address");
        hasError = true;
    }
    
    // validating password
    if (password == null || password.trim().isEmpty()) {
        model.addAttribute("passwordError", "Please enter your password");
        hasError = true;
    } else if (password.length() < 6) {
        model.addAttribute("passwordError", "Password must be at least 6 characters long");
        hasError = true;
    }
    
    if (hasError) {
        return "login";
    }
    
    //checking for 5 login attemps-in one session
    LoginAttempt attempt = loginAttempts.get(identifier);
    if (attempt != null && attempt.attempts >= 5) {
        if (attempt.lastAttempt.plusMinutes(15).isAfter(LocalDateTime.now())) {
            long minutesLeft = Duration.between(LocalDateTime.now(), 
                attempt.lastAttempt.plusMinutes(15)).toMinutes();
            model.addAttribute("loginError", 
                "Too many failed attempts. Please try again in " + 
                minutesLeft + " minutes");
            return "login";
        } else {
            loginAttempts.remove(identifier);
        }
    }
    
    try {
        
        boolean loginSuccessful = userService.validateLogin(identifier, password);
        System.out.println(loginSuccessful);
        if (loginSuccessful) {
            loginAttempts.remove(identifier);
            model.addAttribute("username", identifier);
            
            if ("delete".equals(action)) {
                return "redirect:/manageBookings";
            } else {
                return "redirect:/booking";
            }
        } else {
            if (attempt == null) {
                loginAttempts.put(identifier, new LoginAttempt());
            } else {
                attempt.increment();
            }
            
            model.addAttribute("loginError", "Invalid credentials");
            return "login";
        }
    } catch (Exception e) {
        model.addAttribute("loginError", "An error occurred. Please try again later");
        return "login";
    }
}
    
    private boolean isValidIdentifier(String identifier) {
        return PHONE_PATTERN.matcher(identifier).matches() || 
               EMAIL_PATTERN.matcher(identifier).matches();
    }

    // Show the signup page
    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    // Process signup and redirect to booking form if successful
    @PostMapping("/signup")
    public String processSignup(@RequestParam("username") String username,
                                @RequestParam("email") String email,
                                @RequestParam("password") String password,
                                @RequestParam("mobile") String mobile,
                                Model model) {
        if (userService.createUser(username, email, password, mobile)) {
            return "redirect:/booking";
        } else {
            model.addAttribute("signupError", "Failed to create account. Please try again.");
            return "signup";
        }
    }

    // Show the booking form after login
    @GetMapping("/booking")
    public String showBookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        return "booking-form";
    }

    // Handle booking form submission and redirect to seat selection
    @PostMapping("/submitBooking")
    public String submitForm(@ModelAttribute Booking booking, Model model) {
        model.addAttribute("booking", booking);
        return "redirect:/booking-seat";
    }

    // Show seat selection page
    @GetMapping("/booking-seat")
public String showBookingSeat(@ModelAttribute("booking") Booking booking, Model model) {
    // Add attributes to the model
    model.addAttribute("booking", booking);
    model.addAttribute("busNumber", booking.getBusNumber()); // Assuming busNumber is set in the Booking object
    model.addAttribute("date", booking.getDate());
    this.booking=booking.getBusNumber();
    date=booking.getDate();

    // Print the busNumber and date to the console for debugging
    System.out.println("Bus Number: " + booking.getBusNumber());
    System.out.println("Date: " + booking.getDate());

    return "booking-seat";
}


    // Confirm seat selection and complete booking
    @PostMapping("/confirmSeat")
    public String confirmSeat(@ModelAttribute("booking") Booking booking,
                              @ModelAttribute("username") String username,
                              Model model) {
        if (username == null || username.isEmpty()) {
            model.addAttribute("error", "User is not logged in.");
            return "error";
        }
        
        booking.setUserName(username);
        
        boolean success = bookingService.saveBooking(booking);
        if (success) {
            return "success";
        } else {
            model.addAttribute("error", "Booking failed. Please try again.");
            return "booking-seat";
        }
    }

    // Show manage bookings page with future bookings for deletion
    @GetMapping("/manageBookings")
    public String showManageBookings(@ModelAttribute("username") String username, Model model) {
        List<Booking> bookings = bookingService.getFutureBookings(username, LocalDate.now());
        model.addAttribute("bookings", bookings);
        return "deleting";
    }

    // Delete a booking
    @PostMapping("/deleteBooking")
    public String deleteBooking(@RequestParam("bookingId") Long bookingId,
                                @RequestParam("busNumber") String busNumber,
                                Model model) {
        boolean success = bookingService.deleteBooking(bookingId, busNumber);
        model.addAttribute("success", success ? "Booking deleted successfully." : "Failed to delete booking.");
        return "redirect:/manageBookings";
    }

    @GetMapping("/getBookedSeats")
    @ResponseBody
    public List<String> getBooked() {
        List<String> a=bookingService.getBookedSeats(booking, date);
        System.out.println(a);
        return a;
    
}

@GetMapping("/try")
public String getMethodName(@RequestParam(required=false) String param, Model model) {
    model.addAttribute("param", param);
    return "try1";
}


}
    