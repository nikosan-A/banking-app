package com.gcashapp;

import java.util.function.Predicate;

/**
 * GcashApp – CashIn
 *
 * Implements BankingOperation (polymorphism).
 * Fluent builder lets tests compose operations as lambdas.
 * Java 8: Predicate validation, method chaining.
 */
public class CashIn implements BankingOperation {

    // ── Java 8 Validation Predicates (accessible from tests) ─────────────
    public static final Predicate<Double>  VALID_AMOUNT  = a -> a != null && a >= 1.00;
    public static final Predicate<Integer> VALID_USER_ID = id -> id != null && id > 0;

    private final CheckBalance checkBalance;
    private final Transactions transactions;

    // ── Builder fields ────────────────────────────────────────────────────
    private int    userId;
    private double amount;
    private String description;
    private int    transferFromId;

    public CashIn(CheckBalance checkBalance, Transactions transactions) {
        this.checkBalance = checkBalance;
        this.transactions = transactions;
    }

    // ── Fluent builder (enables BankingOperation polymorphism in tests) ───
    public CashIn forUser(int userId)        { this.userId        = userId;        return this; }
    public CashIn amount(double amount)      { this.amount        = amount;        return this; }
    public CashIn description(String desc)   { this.description   = desc;          return this; }
    public CashIn from(int transferFromId)   { this.transferFromId = transferFromId; return this; }

    @Override public String getOperationName() { return "Cash-In"; }

    @Override
    public boolean execute() {
        return cashIn(userId, amount, description, transferFromId);
    }

    // ── Core method ───────────────────────────────────────────────────────
    public boolean cashIn(int userId, double amount, String description, int transferFromId) {

        if (!VALID_USER_ID.test(userId)) {
            System.out.println("[ERROR] Invalid user ID."); return false; }

        if (!VALID_AMOUNT.test(amount)) {
            System.out.println("[ERROR] Minimum cash-in amount is PHP 1.00."); return false; }

        String desc = (description == null || description.trim().isEmpty())
                ? "Cash-in" : description.trim();

        CheckBalance.Balance bal = checkBalance.findByUserId(userId);
        if (bal == null) {
            System.out.println("[ERROR] Account not found for User ID: " + userId); return false; }

        double prev = bal.getAmount();
        bal.setAmount(prev + amount);
        transactions.addTransaction(amount, desc, userId, userId, transferFromId);

        System.out.printf("[SUCCESS] PHP %.2f added. Balance: PHP %.2f → PHP %.2f%n",
                amount, prev, bal.getAmount());
        return true;
    }
}
