package com.example.banking.controller;

import com.example.banking.model.User;
import com.example.banking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "dashboard"; // renders dashboard.html
    }
}