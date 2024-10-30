package com.example.busticketbooking.controllers;

import com.example.busticketbooking.models.Booking;
import com.example.busticketbooking.services.BookingService;
import com.example.busticketbooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@SessionAttributes({"booking", "username"}) // Adding username to session attributes
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
    public String showLoginForm(Model model) {
        model.addAttribute("loginError", null);
        return "login";
    }

    // Process login and redirect to booking form if successful
    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        if (userService.validateLogin(username, password)) {
            model.addAttribute("username", username); // Store username in session
            return "redirect:/booking";
        } else {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
            return "login";
        }
    }

    // Show signup page
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
            return "redirect:/booking";  // Redirect to booking form upon successful signup
        } else {
            model.addAttribute("signupError", "Failed to create account. Please try again.");
            return "signup";  // Stay on signup page if there was an error
        }
    }

    // Show booking form after login
    @GetMapping("/booking")
    public String showBookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        return "booking-form";
    }

    // Handle booking form submission
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
            return "error"; // Redirect to an error page or show an error message
        }
        
        booking.setUserName(username); // Set the username in the booking
        
        boolean success = bookingService.saveBooking(booking);
        if (success) {
            return "success"; // Redirect to success page
        } else {
            model.addAttribute("error", "Booking failed. Please try again.");
            return "booking-seat"; // Redirect back to seat selection page with error
        }
    }
}
