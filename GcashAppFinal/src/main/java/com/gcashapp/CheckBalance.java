package com.gcashapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * GcashApp – CheckBalance
 *
 * Models the "balance" table (in-memory).
 * Java 8: static Predicate, stream + Optional for lookup.
 *
 * Seeded balances:
 *   user_id 1 → PHP  5,000.00
 *   user_id 2 → PHP 12,500.75
 *   user_id 3 → PHP    300.50
 *   user_id 4 → PHP 99,999.99
 */
public class CheckBalance {

    // ── Inner class: "balance" table row ──────────────────────────────────
    public static class Balance {
        private final int    id;
        private final int    userId;
        private       double amount;

        public Balance(int id, double amount, int userId) {
            this.id = id; this.amount = amount; this.userId = userId;
        }
        public int    getId()     { return id; }
        public double getAmount() { return amount; }
        public int    getUserId() { return userId; }
        public void   setAmount(double amount) { this.amount = amount; }
    }

    // ── Java 8 Predicate: filters balance rows by userId ─────────────────
    public static Predicate<Balance> belongsTo(int userId) {
        return b -> b.getUserId() == userId;
    }

    // ── In-memory table ───────────────────────────────────────────────────
    final List<Balance> balanceTable = new ArrayList<>();

    public CheckBalance() {
        balanceTable.add(new Balance(1,  5000.00,  1));
        balanceTable.add(new Balance(2, 12500.75,  2));
        balanceTable.add(new Balance(3,   300.50,  3));
        balanceTable.add(new Balance(4, 99999.99,  4));
    }

    /** Creates a PHP 0.00 balance record for a newly registered user. */
    public void addBalanceRecord(int userId) {
        balanceTable.add(new Balance(balanceTable.size() + 1, 0.00, userId));
    }

    // ── checkBalance ──────────────────────────────────────────────────────
    public double checkBalance(int userId) {
        if (userId <= 0) { System.out.println("[ERROR] Invalid user ID."); return -1.0; }

        Optional<Balance> record = balanceTable.stream()
                .filter(belongsTo(userId))
                .findFirst();

        if (!record.isPresent()) {
            System.out.println("[ERROR] No balance record for User ID: " + userId);
            return -1.0;
        }
        System.out.printf("  Available Balance: PHP %.2f%n", record.get().getAmount());
        return record.get().getAmount();
    }

    /** Returns a mutable Balance reference (used by CashIn / CashTransfer). */
    public Balance findByUserId(int userId) {
        return balanceTable.stream()
                .filter(belongsTo(userId))
                .findFirst()
                .orElse(null);
    }
}
