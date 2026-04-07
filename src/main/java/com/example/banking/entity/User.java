package com.example.banking.controller;

import com.example.banking.model.User;
import com.example.banking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

    private final UserRepository userRepository;

    public TransactionController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/deposit")
    public String deposit(Authentication authentication, @RequestParam double amount) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(Authentication authentication, @RequestParam double amount) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null && user.getBalance() >= amount) {
            user.setBalance(user.getBalance() - amount);
            userRepository.save(user);
        }
        return "redirect:/dashboard";
    }
}