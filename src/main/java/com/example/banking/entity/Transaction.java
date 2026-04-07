package com.example.banking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "DEPOSIT" or "WITHDRAW"
    private Double amount;
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}