import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp – Transactions (Job Sheet 2-5)
 *
 * Provides three read methods over the shared transaction table:
 *   viewAll()         – every transaction in the table
 *   viewUserAll()     – all transactions belonging to a userID
 *   viewTransaction() – a single transaction by its transactionID
 *
 * Uses an in-memory List (consistent with the rest of GcashApp).
 */
public class Transactions {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Inner class: "transaction" table row ─────────────────────────────
    static class Transaction {
        private int    id;
        private double amount;
        private String name;           // description / label
        private int    accountId;      // owner of this record
        private String date;
        private int    transferToId;
        private int    transferFromId;

        public Transaction(int id, double amount, String name,
                           int accountId, String date,
                           int transferToId, int transferFromId) {
            this.id             = id;
            this.amount         = amount;
            this.name           = name;
            this.accountId      = accountId;
            this.date           = date;
            this.transferToId   = transferToId;
            this.transferFromId = transferFromId;
        }

        public int    getId()             { return id; }
        public double getAmount()         { return amount; }
        public String getName()           { return name; }
        public int    getAccountId()      { return accountId; }
        public String getDate()           { return date; }
        public int    getTransferToId()   { return transferToId; }
        public int    getTransferFromId() { return transferFromId; }

        /** Pretty-print a single transaction row */
        public void print() {
            String direction = transferFromId == 0
                ? "CASH-IN (external)"
                : (transferFromId == accountId
                    ? "SENT    → User #" + transferToId
                    : "RECEIVED ← User #" + transferFromId);

            System.out.printf(
                "  ┌─ TXN #%-4d ──────────────────────────────────%n" +
                "  │  Amount      : PHP %.2f%n"                        +
                "  │  Description : %s%n"                              +
                "  │  Account ID  : %d%n"                              +
                "  │  Date        : %s%n"                              +
                "  │  Direction   : %s%n"                              +
                "  └────────────────────────────────────────────────%n",
                id, amount, name, accountId, date, direction
            );
        }
    }

    // ── In-memory transaction table ───────────────────────────────────────
    private List<Transaction> transactionTable = new ArrayList<>();
    private int               nextId           = 1;

    // ─────────────────────────────────────────────────────────────────────
    // Package-level helper: lets CashIn / CashTransfer insert records
    // ─────────────────────────────────────────────────────────────────────
    void addTransaction(double amount, String name,
                        int accountId, int transferToId, int transferFromId) {
        String now = LocalDateTime.now().format(FORMATTER);
        transactionTable.add(new Transaction(
            nextId++, amount, name, accountId, now, transferToId, transferFromId
        ));
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. viewAll – returns and prints ALL transactions in the table
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Retrieves every transaction record regardless of owner.
     *
     * @return list of all Transaction objects (empty list if none)
     */
    public List<Transaction> viewAll() {
        System.out.println("\n══ VIEW ALL TRANSACTIONS ═══════════════════");

        if (transactionTable.isEmpty()) {
            System.out.println("  (No transactions on record.)\n");
            return new ArrayList<>();
        }

        System.out.printf("  Total records: %d%n%n", transactionTable.size());
        for (Transaction t : transactionTable) {
            t.print();
        }
        return new ArrayList<>(transactionTable);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. viewUserAll – returns all transactions for a specific userID
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Retrieves every transaction that belongs to {@code userId}.
     *
     * @param  userId  the account owner's ID
     * @return list of matching Transaction objects (empty list if none)
     */
    public List<Transaction> viewUserAll(int userId) {
        System.out.println("\n══ VIEW TRANSACTIONS FOR USER ID: " + userId +
                           " ══════════");

        // ── Validate input ────────────────────────────────────────────────
        if (userId <= 0) {
            System.out.println("  [ERROR] Invalid user ID: " + userId + "\n");
            return new ArrayList<>();
        }

        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactionTable) {
            if (t.getAccountId() == userId) {
                results.add(t);
            }
        }

        if (results.isEmpty()) {
            System.out.println("  (No transactions found for User ID: " + userId + ")\n");
            return results;
        }

        System.out.printf("  Records found: %d%n%n", results.size());
        for (Transaction t : results) {
            t.print();
        }
        return results;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. viewTransaction – returns a single transaction by transactionID
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the transaction identified by {@code transactionId}.
     *
     * @param  transactionId  the unique ID of the transaction
     * @return the matching Transaction, or null if not found
     */
    public Transaction viewTransaction(int transactionId) {
        System.out.println("\n══ VIEW TRANSACTION ID: " + transactionId +
                           " ══════════════════════");

        // ── Validate input ────────────────────────────────────────────────
        if (transactionId <= 0) {
            System.out.println("  [ERROR] Invalid transaction ID: " + transactionId + "\n");
            return null;
        }

        for (Transaction t : transactionTable) {
            if (t.getId() == transactionId) {
                t.print();
                return t;
            }
        }

        System.out.println("  [ERROR] No transaction found with ID: " + transactionId + "\n");
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────
    // MAIN – Demo: seed transactions then exercise all three view methods
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("  GcashApp – Transactions Module      ");
        System.out.println("======================================");

        // ── Build shared infrastructure ───────────────────────────────────
        Transactions txns = new Transactions();

        // ── Seed data: simulate cash-ins and transfers that occurred ───────
        // (In the full app these are created by CashIn / CashTransfer)
        txns.addTransaction(200.00,  "Cash-in via 7-Eleven",       1, 1, 0);
        txns.addTransaction(300.00,  "Cash-in via Palawan Express", 1, 1, 0);
        txns.addTransaction(1500.00, "Cash-in via BDO",             2, 2, 0);

        // Juan (1) → Maria (2) PHP 1,000
        txns.addTransaction(1000.00, "SENT – Bayad sa utang",      1, 2, 1);
        txns.addTransaction(1000.00, "RECEIVED – Bayad sa utang",  2, 2, 1);

        // Juan (1) → Pedro (3) PHP 500
        txns.addTransaction(500.00,  "SENT – Pasalubong money",    1, 3, 1);
        txns.addTransaction(500.00,  "RECEIVED – Pasalubong money",3, 3, 1);

        // Maria (2) cash-in
        txns.addTransaction(750.00,  "Cash-in via GCash partner",  2, 2, 0);

        // ─────────────────────────────────────────────────────────────────
        // Method 1: viewAll
        // ─────────────────────────────────────────────────────────────────
        txns.viewAll();

        // ─────────────────────────────────────────────────────────────────
        // Method 2: viewUserAll
        // ─────────────────────────────────────────────────────────────────
        txns.viewUserAll(1);    // Juan's transactions
        txns.viewUserAll(2);    // Maria's transactions
        txns.viewUserAll(3);    // Pedro's transactions
        txns.viewUserAll(99);   // Non-existent user
        txns.viewUserAll(-5);   // Invalid ID

        // ─────────────────────────────────────────────────────────────────
        // Method 3: viewTransaction
        // ─────────────────────────────────────────────────────────────────
        txns.viewTransaction(1);   // First cash-in
        txns.viewTransaction(4);   // Juan → Maria SENT record
        txns.viewTransaction(5);   // Juan → Maria RECEIVED record
        txns.viewTransaction(99);  // Non-existent
        txns.viewTransaction(-1);  // Invalid

        System.out.println("======================================");
        System.out.println("           End of Demo                ");
        System.out.println("======================================");
    }
}