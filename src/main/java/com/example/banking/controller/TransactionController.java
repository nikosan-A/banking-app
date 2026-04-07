package com.example.banking.controller;

import com.example.banking.entity.User;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.UserRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionController(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/deposit")
    public String deposit(Authentication authentication, @RequestParam double amount, Model model) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            // Update balance
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            // Create transaction record
            Transaction tx = new Transaction();
            tx.setType("DEPOSIT");
            tx.setAmount(amount);
            tx.setUser(user);
            transactionRepository.save(tx);

            // Pass data to receipt page
            model.addAttribute("user", user);
            model.addAttribute("transaction", tx);
            return "receipt";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(Authentication authentication, @RequestParam double amount, Model model) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null && user.getBalance() >= amount) {
            // Update balance
            user.setBalance(user.getBalance() - amount);
            userRepository.save(user);

            // Create transaction record
            Transaction tx = new Transaction();
            tx.setType("WITHDRAW");
            tx.setAmount(amount);
            tx.setUser(user);
            transactionRepository.save(tx);

            // Pass data to receipt page
            model.addAttribute("user", user);
            model.addAttribute("transaction", tx);
            return "receipt";
        }
        return "redirect:/dashboard";
    }
}