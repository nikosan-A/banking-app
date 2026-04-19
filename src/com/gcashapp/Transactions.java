package com.gcashapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp – Transactions
 * Shared transaction log used by CashIn and CashTransfer.
 * Provides viewAll(), viewUserAll(userId), viewTransaction(txnId).
 */
public class Transactions {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Inner class: "transaction" table row ─────────────────────────────
    public static class Transaction {
        private int    id;
        private double amount;
        private String name;
        private int    accountId;
        private String date;
        private int    transferToId;
        private int    transferFromId;

        public Transaction(int id, double amount, String name,
                           int accountId, String date,
                           int transferToId, int transferFromId) {
            this.id = id; this.amount = amount; this.name = name;
            this.accountId = accountId; this.date = date;
            this.transferToId = transferToId; this.transferFromId = transferFromId;
        }

        public int    getId()             { return id; }
        public double getAmount()         { return amount; }
        public String getName()           { return name; }
        public int    getAccountId()      { return accountId; }
        public String getDate()           { return date; }
        public int    getTransferToId()   { return transferToId; }
        public int    getTransferFromId() { return transferFromId; }

        public void print() {
            String dir = transferFromId == 0 ? "CASH-IN (external)"
                : (transferFromId == accountId
                    ? "SENT    → User #" + transferToId
                    : "RECEIVED ← User #" + transferFromId);
            System.out.printf(
                "  [#%-3d] PHP %9.2f | %-32s | %s | %s%n",
                id, amount, name, date, dir);
        }
    }

    private List<Transaction> transactionTable = new ArrayList<>();
    private int               nextId           = 1;

    // ── Called by CashIn / CashTransfer ───────────────────────────────────
    public void addTransaction(double amount, String name,
                               int accountId, int transferToId, int transferFromId) {
        String now = LocalDateTime.now().format(FORMATTER);
        transactionTable.add(new Transaction(
            nextId++, amount, name, accountId, now, transferToId, transferFromId));
    }

    // ── viewAll ───────────────────────────────────────────────────────────
    public List<Transaction> viewAll() {
        System.out.println("\n  ── ALL TRANSACTIONS (" + transactionTable.size() + " records) ──");
        if (transactionTable.isEmpty()) {
            System.out.println("  (No transactions on record.)");
            return new ArrayList<>();
        }
        for (Transaction t : transactionTable) t.print();
        return new ArrayList<>(transactionTable);
    }

    // ── viewUserAll ───────────────────────────────────────────────────────
    public List<Transaction> viewUserAll(int userId) {
        if (userId <= 0) {
            System.out.println("  [ERROR] Invalid user ID."); return new ArrayList<>(); }
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactionTable)
            if (t.getAccountId() == userId) results.add(t);

        System.out.println("\n  ── TRANSACTIONS FOR USER #" + userId +
                           " (" + results.size() + " records) ──");
        if (results.isEmpty()) System.out.println("  (No transactions found.)");
        else for (Transaction t : results) t.print();
        return results;
    }

    // ── viewTransaction ───────────────────────────────────────────────────
    public Transaction viewTransaction(int txnId) {
        if (txnId <= 0) {
            System.out.println("  [ERROR] Invalid transaction ID."); return null; }
        for (Transaction t : transactionTable) {
            if (t.getId() == txnId) { t.print(); return t; }
        }
        System.out.println("  [ERROR] Transaction #" + txnId + " not found.");
        return null;
    }
}
