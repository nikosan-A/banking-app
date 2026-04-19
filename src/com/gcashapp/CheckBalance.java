package com.gcashapp;

import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp – CheckBalance
 * Models the "balance" table and provides checkBalance(userId).
 */
public class CheckBalance {

    // ── Inner class: "balance" table row ──────────────────────────────────
    public static class Balance {
        private int    id;
        private double amount;
        private int    userId;

        public Balance(int id, double amount, int userId) {
            this.id = id; this.amount = amount; this.userId = userId;
        }

        public int    getId()     { return id; }
        public double getAmount() { return amount; }
        public int    getUserId() { return userId; }
        public void   setAmount(double amount) { this.amount = amount; }
    }

    List<Balance> balanceTable = new ArrayList<>();

    // ── Constructor: seed dummy balances matching the 4 demo users ────────
    public CheckBalance() {
        balanceTable.add(new Balance(1,  5000.00,  1));
        balanceTable.add(new Balance(2, 12500.75,  2));
        balanceTable.add(new Balance(3,   300.50,  3));
        balanceTable.add(new Balance(4, 99999.99,  4));
    }

    // ── Add a new balance record for a newly registered user ──────────────
    public void addBalanceRecord(int userId) {
        balanceTable.add(new Balance(balanceTable.size() + 1, 0.00, userId));
    }

    // ── checkBalance ──────────────────────────────────────────────────────
    public double checkBalance(int userId) {
        if (userId <= 0) {
            System.out.println("[ERROR] Invalid user ID."); return -1.0; }
        for (Balance b : balanceTable) {
            if (b.getUserId() == userId) {
                System.out.printf("  Available Balance: PHP %.2f%n", b.getAmount());
                return b.getAmount();
            }
        }
        System.out.println("[ERROR] No balance record found for User ID: " + userId);
        return -1.0;
    }

    // ── Internal helper ───────────────────────────────────────────────────
    public Balance findByUserId(int userId) {
        for (Balance b : balanceTable) {
            if (b.getUserId() == userId) return b;
        }
        return null;
    }
}
