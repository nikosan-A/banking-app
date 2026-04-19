package com.gcashapp;

/**
 * GcashApp – CashIn
 * Adds cash to a user's balance and logs the transaction.
 */
public class CashIn {

    private CheckBalance checkBalance;
    private Transactions transactions;

    public CashIn(CheckBalance checkBalance, Transactions transactions) {
        this.checkBalance = checkBalance;
        this.transactions = transactions;
    }

    // ── cashIn ────────────────────────────────────────────────────────────
    public boolean cashIn(int userId, double amount, String description, int transferFromId) {
        if (userId <= 0) {
            System.out.println("[ERROR] Invalid user ID."); return false; }
        if (amount < 1.00) {
            System.out.println("[ERROR] Minimum cash-in amount is PHP 1.00."); return false; }
        if (description == null || description.trim().isEmpty()) description = "Cash-in";

        CheckBalance.Balance bal = checkBalance.findByUserId(userId);
        if (bal == null) {
            System.out.println("[ERROR] Account not found for User ID: " + userId); return false; }

        double prev = bal.getAmount();
        bal.setAmount(prev + amount);
        transactions.addTransaction(amount, description.trim(),
                                    userId, userId, transferFromId);

        System.out.printf("[SUCCESS] PHP %.2f added. Balance: PHP %.2f → PHP %.2f%n",
                          amount, prev, bal.getAmount());
        return true;
    }
}
