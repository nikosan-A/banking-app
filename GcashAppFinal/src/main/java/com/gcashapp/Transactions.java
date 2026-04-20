package com.gcashapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * GcashApp – Transactions
 *
 * Shared transaction log for CashIn and CashTransfer.
 * Java 8: static Predicates, stream, Optional, method references.
 *
 * Methods:
 *   viewAll()              – all transactions
 *   viewUserAll(userId)    – transactions for one user
 *   viewTransaction(txnId) – single transaction by ID
 *   query(predicate)       – custom Predicate query
 */
public class Transactions {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Inner class: "transaction" table row ─────────────────────────────
    public static class Transaction {
        private final int    id;
        private final double amount;
        private final String name;
        private final int    accountId;
        private final String date;
        private final int    transferToId;
        private final int    transferFromId;

        public Transaction(int id, double amount, String name, int accountId,
                           String date, int transferToId, int transferFromId) {
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
            String dir = transferFromId == 0
                    ? "CASH-IN (external)"
                    : (transferFromId == accountId
                        ? "SENT    → User #" + transferToId
                        : "RECEIVED ← User #" + transferFromId);
            System.out.printf("  [#%-3d] PHP %9.2f | %-35s | %s | %s%n",
                    id, amount, name, date, dir);
        }
    }

    // ── Reusable Java 8 Predicates ────────────────────────────────────────
    public static Predicate<Transaction> byUser(int userId) {
        return t -> t.getAccountId() == userId;
    }
    public static Predicate<Transaction> isCashIn() {
        return t -> t.getTransferFromId() == 0;
    }
    public static Predicate<Transaction> isSent() {
        return t -> t.getName().startsWith("SENT");
    }
    public static Predicate<Transaction> isReceived() {
        return t -> t.getName().startsWith("RECEIVED");
    }
    public static Predicate<Transaction> amountGreaterThan(double threshold) {
        return t -> t.getAmount() > threshold;
    }

    // ── State ─────────────────────────────────────────────────────────────
    private final List<Transaction> transactionTable = new ArrayList<>();
    private       int               nextId           = 1;

    /** Called by CashIn / CashTransfer after a successful operation. */
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
        transactionTable.forEach(Transaction::print);   // method reference
        return new ArrayList<>(transactionTable);
    }

    // ── viewUserAll ───────────────────────────────────────────────────────
    public List<Transaction> viewUserAll(int userId) {
        if (userId <= 0) {
            System.out.println("  [ERROR] Invalid user ID.");
            return new ArrayList<>();
        }
        List<Transaction> results = transactionTable.stream()
                .filter(byUser(userId))
                .collect(Collectors.toList());

        System.out.println("\n  ── TRANSACTIONS FOR USER #" + userId +
                           " (" + results.size() + " records) ──");
        results.forEach(Transaction::print);
        return results;
    }

    // ── viewTransaction ───────────────────────────────────────────────────
    public Transaction viewTransaction(int txnId) {
        if (txnId <= 0) {
            System.out.println("  [ERROR] Invalid transaction ID.");
            return null;
        }
        Optional<Transaction> found = transactionTable.stream()
                .filter(t -> t.getId() == txnId)
                .findFirst();

        found.ifPresent(Transaction::print);
        if (!found.isPresent()) System.out.println("  [ERROR] Transaction #" + txnId + " not found.");
        return found.orElse(null);
    }

    /** Flexible query using any Predicate<Transaction>. */
    public List<Transaction> query(Predicate<Transaction> predicate) {
        return transactionTable.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
