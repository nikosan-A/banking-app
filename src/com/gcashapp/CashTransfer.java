package com.gcashapp;

/**
 * GcashApp – CashTransfer
 * Transfers funds between two user accounts.
 */
public class CashTransfer {

    private static final double MAX_TRANSFER = 50_000.00;
    private static final double MIN_TRANSFER =      1.00;

    private CheckBalance checkBalance;
    private Transactions transactions;

    public CashTransfer(CheckBalance checkBalance, Transactions transactions) {
        this.checkBalance = checkBalance;
        this.transactions = transactions;
    }

    // ── cashTransfer ──────────────────────────────────────────────────────
    public boolean cashTransfer(int senderId, int recipientId,
                                double amount, String note) {
        if (senderId <= 0) {
            System.out.println("[ERROR] Invalid sender ID."); return false; }
        if (recipientId == senderId) {
            System.out.println("[ERROR] Cannot transfer to your own account."); return false; }
        if (recipientId <= 0) {
            System.out.println("[ERROR] Invalid recipient ID."); return false; }
        if (amount < MIN_TRANSFER) {
            System.out.printf("[ERROR] Minimum transfer is PHP %.2f%n", MIN_TRANSFER); return false; }
        if (amount > MAX_TRANSFER) {
            System.out.printf("[ERROR] Maximum transfer is PHP %.2f%n", MAX_TRANSFER); return false; }
        if (note == null || note.trim().isEmpty()) note = "GCash Transfer";
        if (note.length() > 50) {
            System.out.println("[ERROR] Note must not exceed 50 characters."); return false; }

        CheckBalance.Balance senderBal    = checkBalance.findByUserId(senderId);
        CheckBalance.Balance recipientBal = checkBalance.findByUserId(recipientId);

        if (senderBal == null) {
            System.out.println("[ERROR] Sender account not found."); return false; }
        if (recipientBal == null) {
            System.out.println("[ERROR] Recipient account (ID: " + recipientId + ") not found."); return false; }
        if (senderBal.getAmount() < amount) {
            System.out.printf("[ERROR] Insufficient balance. Available: PHP %.2f%n",
                              senderBal.getAmount()); return false; }

        double senderPrev    = senderBal.getAmount();
        double recipientPrev = recipientBal.getAmount();

        senderBal.setAmount(senderPrev - amount);
        recipientBal.setAmount(recipientPrev + amount);

        transactions.addTransaction(amount, "SENT – "     + note.trim(), senderId,    recipientId, senderId);
        transactions.addTransaction(amount, "RECEIVED – " + note.trim(), recipientId, recipientId, senderId);

        System.out.printf("[SUCCESS] PHP %.2f sent to User #%d. Your new balance: PHP %.2f%n",
                          amount, recipientId, senderBal.getAmount());
        return true;
    }
}
