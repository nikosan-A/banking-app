import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp – CashIn
 *
 * Adds cash to a user's balance and records it in the "transaction" table.
 * Uses in-memory Lists — no external JDBC driver required.
 */
public class CashIn {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Inner class: "transaction" table row ─────────────────────────────
    static class Transaction {
        private int    id;
        private double amount;
        private String name;          // description / label
        private int    accountId;
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
    }

    // ── In-memory "transaction" table & shared balance table ─────────────
    private List<Transaction>          transactionTable = new ArrayList<>();
    private int                        nextTxnId        = 1;
    private CheckBalance               checkBalance;    // shared balance store

    // ── Constructor: receives a CheckBalance instance to share its data ──
    public CashIn(CheckBalance checkBalance) {
        this.checkBalance = checkBalance;
        System.out.println("[DB] Transaction table initialized.\n");
    }

    // ─────────────────────────────────────────────────────────────────────
    // cashIn – credits amount to balance and logs the transaction
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Credits {@code amount} to the account of {@code userId}.
     *
     * @param userId         ID of the account receiving the cash-in
     * @param amount         amount to add (must be > 0)
     * @param description    transaction label (e.g. "Cash-in via 7-Eleven")
     * @param transferFromId source/sender ID; 0 = external / over-the-counter
     * @return true if successful, false otherwise
     */
    public boolean cashIn(int userId, double amount,
                          String description, int transferFromId) {

        System.out.println("── CASH IN ────────────────────────────────");

        // ── Validation ────────────────────────────────────────────────────
        if (userId <= 0) {
            System.out.println("[ERROR] Invalid user ID: " + userId + "\n");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[ERROR] Cash-in amount must be greater than 0.\n");
            return false;
        }
        if (amount < 1.00) {
            System.out.println("[ERROR] Minimum cash-in amount is PHP 1.00.\n");
            return false;
        }
        if (description == null || description.trim().isEmpty()) {
            description = "Cash-in";
        }

        // ── Find the user's balance record ────────────────────────────────
        CheckBalance.Balance balanceRecord = checkBalance.findByUserId(userId);
        if (balanceRecord == null) {
            System.out.println("[ERROR] No account found for user ID: " + userId + "\n");
            return false;
        }

        double previousBalance = balanceRecord.getAmount();
        double newBalance      = previousBalance + amount;
        String now             = LocalDateTime.now().format(FORMATTER);

        // ── Update balance ────────────────────────────────────────────────
        balanceRecord.setAmount(newBalance);

        // ── Log transaction ───────────────────────────────────────────────
        Transaction txn = new Transaction(
            nextTxnId++, amount, description.trim(),
            userId, now, userId, transferFromId
        );
        transactionTable.add(txn);

        // ── Receipt ───────────────────────────────────────────────────────
        System.out.println("[SUCCESS] Cash-in completed!");
        System.out.printf ("          User ID         : %d%n",       userId);
        System.out.printf ("          Amount Added    : PHP %.2f%n",  amount);
        System.out.printf ("          Previous Balance: PHP %.2f%n",  previousBalance);
        System.out.printf ("          New Balance     : PHP %.2f%n",  newBalance);
        System.out.printf ("          Description     : %s%n",        description.trim());
        System.out.printf ("          Date            : %s%n%n",      now);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Utility – print all transactions for a user
    // ─────────────────────────────────────────────────────────────────────
    public void printTransactionHistory(int userId) {
        System.out.println("── TRANSACTION HISTORY (User ID: " + userId + ") ──");

        boolean hasRecords = false;
        for (Transaction t : transactionTable) {
            if (t.getAccountId() == userId) {
                hasRecords = true;
                System.out.printf(
                    "  [TXN #%d] PHP %.2f | %-28s | %s | From: %s%n",
                    t.getId(),
                    t.getAmount(),
                    t.getName(),
                    t.getDate(),
                    t.getTransferFromId() == 0
                        ? "External/OTC"
                        : "User #" + t.getTransferFromId()
                );
            }
        }
        if (!hasRecords) System.out.println("  (No transactions found.)");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────
    // MAIN – Demo (cashIn 200 then 300 as required by Step 4)
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("    GcashApp – Cash-In Module         ");
        System.out.println("======================================\n");

        // Shared balance store (seeded with dummy data)
        CheckBalance cb = new CheckBalance();
        CashIn ci = new CashIn(cb);

        System.out.println("── Initial balance of User 1 ──────────────");
        cb.checkBalance(1);

        // ── Step 4: Test cash-in PHP 200, then PHP 300 for User 1 ─────────
        ci.cashIn(1, 200.00, "Cash-in via 7-Eleven",        0);
        ci.cashIn(1, 300.00, "Cash-in via Palawan Express",  0);

        // ── Additional test: another user ─────────────────────────────────
        ci.cashIn(2, 1500.00, "Cash-in via BDO", 0);

        // ── Edge cases ────────────────────────────────────────────────────
        ci.cashIn(1,  -50.00, "Negative amount", 0);   // rejected
        ci.cashIn(99, 100.00, "Unknown user",    0);   // rejected

        // ── Final balances ────────────────────────────────────────────────
        System.out.println("── Final Balances ──────────────────────────");
        cb.checkBalance(1);   // Juan: 5000 + 200 + 300 = PHP 5,500.00
        cb.checkBalance(2);   // Maria: 12500.75 + 1500 = PHP 14,000.75

        // ── Transaction history ───────────────────────────────────────────
        ci.printTransactionHistory(1);
        ci.printTransactionHistory(2);

        System.out.println("======================================");
        System.out.println("           End of Demo                ");
        System.out.println("======================================");
    }
}