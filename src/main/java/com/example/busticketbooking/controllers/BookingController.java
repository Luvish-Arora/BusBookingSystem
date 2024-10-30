package com.example.busticketbooking.controllers;

import com.example.busticketbooking.models.Booking;
import com.example.busticketbooking.services.BookingService;
import com.example.busticketbooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@SessionAttributes({"booking", "username", "action"}) // Store booking, username, and action in session
public class BookingController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    // Show the index page
    @GetMapping("/")
    public String showIndex(Model model) {
        return "index";
    }

    // Show the login page
    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "action", required = false) String action) {
        model.addAttribute("loginError", null);
        model.addAttribute("action", action); // Pass action to the login form
        return "login";
    }

    // Process login and redirect to the appropriate page based on the action
    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam(value = "action", required = false) String action,
                               Model model) {
        if (userService.validateLogin(username, password)) {
            model.addAttribute("username", username); // Store username in session
            model.addAttribute("action", action); // Store action in session

            // Check if 'action' parameter is set to 'delete' and redirect accordingly
            if ("delete".equals(action)) {
                return "redirect:/manageBookings"; // Redirect to manage bookings
            } else {
                return "redirect:/booking"; // Redirect to booking form
            }
        } else {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
            return "login";
        }
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
        model.addAttribute("booking", booking);
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
}
    