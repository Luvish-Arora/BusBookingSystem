package com.example.busticketbooking.controllers;

import com.example.busticketbooking.models.Booking;
import com.example.busticketbooking.services.BookingService;
import com.example.busticketbooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes("booking")
public class BookingController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService; // Inject BookingService

    // Show the index page
    @GetMapping("/")
    public String showIndex(Model model) {
        return "index";
    }

    // Show the login page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        // Ensure the error message is not carried over from previous failed login attempts
        model.addAttribute("loginError", null);
        return "login";
    }

    // Handle login submission and redirect to booking form if successful
    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        // Validate username and password using UserService
        if (userService.validateLogin(username, password)) {
            return "redirect:/booking";  // Redirect to booking form if successful
        } else {
            // Add error message to the model to display on the login page
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
            return "login";  // Stay on login page if login fails
        }
    }

    // Show the booking form after successful login
    @GetMapping("/booking")
    public String showBookingForm(Model model) {
        // Add a new booking object to the model to bind the form data
        model.addAttribute("booking", new Booking());
        return "booking-form";
    }

    // Handle the form submission for booking details
    @PostMapping("/submitBooking")
    public String submitForm(@ModelAttribute Booking booking, Model model) {
        model.addAttribute("booking", booking);  // Add the booking to the session attributes
        return "redirect:/booking-seat";  // Redirect to seat selection page
    }

    // Show the seat selection page
    @GetMapping("/booking-seat")
    public String showBookingSeat(@ModelAttribute("booking") Booking booking, Model model) {
        model.addAttribute("booking", booking);
        return "booking-seat";  // Render the seat selection page
    }

    // Handle seat confirmation and complete the booking
    @PostMapping("/confirmSeat")
    public String confirmSeat(@ModelAttribute("booking") Booking booking, Model model, SessionStatus status) {
        // Save the booking details using BookingService
        bookingService.saveBooking(booking);
        // Mark the session as complete
        status.setComplete();
        return "success";  // Show the success page after booking is completed
    }
}
