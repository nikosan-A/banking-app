package com.gcashapp;

import java.util.function.Predicate;

/**
 * GcashApp – CashTransfer
 *
 * Implements BankingOperation (polymorphism).
 * Java 8: Predicate validation, fluent builder.
 *
 * Restrictions:
 *   1. Must not transfer to self
 *   2. Amount: PHP 1.00 – PHP 50,000.00
 *   3. Note max 50 characters
 *   4. Sender & recipient must exist
 *   5. Sender must have sufficient balance
 */
public class CashTransfer implements BankingOperation {

    public static final double MAX_TRANSFER = 50_000.00;
    public static final double MIN_TRANSFER =      1.00;

    // ── Java 8 Validation Predicates ─────────────────────────────────────
    public static final Predicate<Double> VALID_AMOUNT =
            a -> a != null && a >= MIN_TRANSFER && a <= MAX_TRANSFER;

    public static final Predicate<String> VALID_NOTE =
            n -> n != null && n.trim().length() <= 50;

    private final CheckBalance checkBalance;
    private final Transactions transactions;

    // ── Builder fields ────────────────────────────────────────────────────
    private int    senderId;
    private int    recipientId;
    private double amount;
    private String note;

    public CashTransfer(CheckBalance checkBalance, Transactions transactions) {
        this.checkBalance = checkBalance;
        this.transactions = transactions;
    }

    // ── Fluent builder ────────────────────────────────────────────────────
    public CashTransfer from(int senderId)     { this.senderId    = senderId;    return this; }
    public CashTransfer to(int recipientId)    { this.recipientId = recipientId; return this; }
    public CashTransfer amount(double amount)  { this.amount      = amount;      return this; }
    public CashTransfer note(String note)      { this.note        = note;        return this; }

    @Override public String getOperationName() { return "Cash-Transfer"; }

    @Override
    public boolean execute() {
        return cashTransfer(senderId, recipientId, amount, note);
    }

    // ── Core method ───────────────────────────────────────────────────────
    public boolean cashTransfer(int senderId, int recipientId, double amount, String note) {

        if (senderId <= 0)               { System.out.println("[ERROR] Invalid sender ID.");                           return false; }
        if (recipientId == senderId)     { System.out.println("[ERROR] Cannot transfer to your own account.");         return false; }
        if (recipientId <= 0)            { System.out.println("[ERROR] Invalid recipient ID.");                        return false; }
        if (!VALID_AMOUNT.test(amount))  { System.out.printf("[ERROR] Amount must be PHP %.2f–%.2f%n",
                                                               MIN_TRANSFER, MAX_TRANSFER);                            return false; }
        if (!VALID_NOTE.test(note))      { System.out.println("[ERROR] Note must not exceed 50 characters.");          return false; }

        String memo = (note == null || note.trim().isEmpty()) ? "GCash Transfer" : note.trim();

        CheckBalance.Balance senderBal    = checkBalance.findByUserId(senderId);
        CheckBalance.Balance recipientBal = checkBalance.findByUserId(recipientId);

        if (senderBal    == null) { System.out.println("[ERROR] Sender account not found.");                              return false; }
        if (recipientBal == null) { System.out.println("[ERROR] Recipient (ID: " + recipientId + ") not found.");        return false; }
        if (senderBal.getAmount() < amount) {
            System.out.printf("[ERROR] Insufficient balance. Available: PHP %.2f%n", senderBal.getAmount()); return false; }

        double sp = senderBal.getAmount(), rp = recipientBal.getAmount();
        senderBal.setAmount(sp - amount);
        recipientBal.setAmount(rp + amount);

        transactions.addTransaction(amount, "SENT – "     + memo, senderId,    recipientId, senderId);
        transactions.addTransaction(amount, "RECEIVED – " + memo, recipientId, recipientId, senderId);

        System.out.printf("[SUCCESS] PHP %.2f sent to User #%d. Your balance: PHP %.2f%n",
                amount, recipientId, senderBal.getAmount());
        return true;
    }
}
