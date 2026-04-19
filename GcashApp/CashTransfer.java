import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp – CashTransfer
 *
 * Transfers cash from one user account to another.
 * - Deducts the amount from the sender's balance
 * - Adds the amount to the recipient's balance
 * - Logs two transaction records (one per side)
 *
 * Uses in-memory Lists (consistent with UserAuthentication,
 * CheckBalance, and CashIn) — no external driver required.
 */
public class CashTransfer {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Maximum single transfer limit (GCash-style rule) ─────────────────
    private static final double MAX_TRANSFER   = 50_000.00;
    private static final double MIN_TRANSFER   =      1.00;

    // ── Inner class: "transaction" table row ─────────────────────────────
    static class Transaction {
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

    // ── Shared references ─────────────────────────────────────────────────
    private CheckBalance            checkBalance;
    private List<Transaction>       transactionTable = new ArrayList<>();
    private int                     nextTxnId        = 1;

    // ── Currently logged-in user (set by login) ───────────────────────────
    private int    loggedInUserId   = -1;
    private String loggedInUserName = "";

    public CashTransfer(CheckBalance checkBalance) {
        this.checkBalance = checkBalance;
    }

    // ── Simulate session (call after UserAuthentication.login()) ──────────
    public void setSession(int userId, String userName) {
        this.loggedInUserId   = userId;
        this.loggedInUserName = userName;
    }

    // ─────────────────────────────────────────────────────────────────────
    // cashTransfer – moves amount from sender (logged-in) to recipient
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Transfers {@code amount} from the currently logged-in user
     * to the account identified by {@code recipientId}.
     *
     * @param recipientId  ID of the account to receive the funds
     * @param amount       amount to send (PHP 1.00 – PHP 50,000.00)
     * @param note         optional transfer note / memo
     * @return true if transfer succeeded, false otherwise
     */
    public boolean cashTransfer(int recipientId, double amount, String note) {

        System.out.println("── CASH TRANSFER ──────────────────────────");

        // ── Restriction 1: Must be logged in ──────────────────────────────
        if (loggedInUserId <= 0) {
            System.out.println("[ERROR] No active session. Please log in first.\n");
            return false;
        }

        // ── Restriction 2: Cannot transfer to yourself ────────────────────
        if (recipientId == loggedInUserId) {
            System.out.println("[ERROR] You cannot transfer funds to your own account.\n");
            return false;
        }

        // ── Restriction 3: Valid recipient ID ─────────────────────────────
        if (recipientId <= 0) {
            System.out.println("[ERROR] Invalid recipient ID: " + recipientId + "\n");
            return false;
        }

        // ── Restriction 4: Amount range ───────────────────────────────────
        if (amount < MIN_TRANSFER) {
            System.out.printf("[ERROR] Minimum transfer amount is PHP %.2f%n%n", MIN_TRANSFER);
            return false;
        }
        if (amount > MAX_TRANSFER) {
            System.out.printf("[ERROR] Maximum single transfer limit is PHP %.2f%n%n", MAX_TRANSFER);
            return false;
        }

        // ── Restriction 5: Sender must have a balance record ─────────────
        CheckBalance.Balance senderBalance = checkBalance.findByUserId(loggedInUserId);
        if (senderBalance == null) {
            System.out.println("[ERROR] Sender account not found.\n");
            return false;
        }

        // ── Restriction 6: Recipient must exist ───────────────────────────
        CheckBalance.Balance recipientBalance = checkBalance.findByUserId(recipientId);
        if (recipientBalance == null) {
            System.out.println("[ERROR] Recipient account (ID: " + recipientId + ") not found.\n");
            return false;
        }

        // ── Restriction 7: Sufficient balance ─────────────────────────────
        if (senderBalance.getAmount() < amount) {
            System.out.printf(
                "[ERROR] Insufficient balance. Available: PHP %.2f | Requested: PHP %.2f%n%n",
                senderBalance.getAmount(), amount);
            return false;
        }

        // ── Restriction 8: Note length ────────────────────────────────────
        if (note == null || note.trim().isEmpty()) note = "GCash Transfer";
        if (note.length() > 50) {
            System.out.println("[ERROR] Transfer note must not exceed 50 characters.\n");
            return false;
        }

        // ── Execute transfer ──────────────────────────────────────────────
        double senderPrev    = senderBalance.getAmount();
        double recipientPrev = recipientBalance.getAmount();
        String now           = LocalDateTime.now().format(FORMATTER);

        senderBalance.setAmount(senderPrev - amount);
        recipientBalance.setAmount(recipientPrev + amount);

        // ── Log DEBIT transaction (sender side) ───────────────────────────
        transactionTable.add(new Transaction(
            nextTxnId++, amount,
            "SENT – " + note.trim(),
            loggedInUserId, now,
            recipientId,    loggedInUserId
        ));

        // ── Log CREDIT transaction (recipient side) ───────────────────────
        transactionTable.add(new Transaction(
            nextTxnId++, amount,
            "RECEIVED – " + note.trim(),
            recipientId, now,
            recipientId, loggedInUserId
        ));

        // ── Receipt ───────────────────────────────────────────────────────
        System.out.println("[SUCCESS] Transfer completed!");
        System.out.printf ("          Sender   (ID %d): PHP %.2f  →  PHP %.2f%n",
                            loggedInUserId, senderPrev, senderBalance.getAmount());
        System.out.printf ("          Recipient(ID %d): PHP %.2f  →  PHP %.2f%n",
                            recipientId, recipientPrev, recipientBalance.getAmount());
        System.out.printf ("          Amount Transferred : PHP %.2f%n", amount);
        System.out.printf ("          Note               : %s%n", note.trim());
        System.out.printf ("          Date               : %s%n%n", now);
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
                    "  [TXN #%d] PHP %8.2f | %-35s | %s%n",
                    t.getId(), t.getAmount(), t.getName(), t.getDate()
                );
            }
        }
        if (!hasRecords) System.out.println("  (No transactions found.)");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────
    // MAIN – Demo / Test scenarios
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("   GcashApp – Cash Transfer Module    ");
        System.out.println("======================================\n");

        // ── Setup shared balance store ────────────────────────────────────
        CheckBalance cb = new CheckBalance();
        CashTransfer ct = new CashTransfer(cb);

        System.out.println("── Initial Balances ────────────────────────");
        cb.checkBalance(1);   // Juan   – PHP 5,000.00
        cb.checkBalance(2);   // Maria  – PHP 12,500.75
        cb.checkBalance(3);   // Pedro  – PHP 300.50

        // ── Simulate Juan logging in ──────────────────────────────────────
        ct.setSession(1, "Juan dela Cruz");

        // ── Scenario 1: Valid transfer PHP 1,000 → Maria ─────────────────
        ct.cashTransfer(2, 1000.00, "Bayad sa utang");

        // ── Scenario 2: Valid transfer PHP 500 → Pedro ────────────────────
        ct.cashTransfer(3, 500.00, "Pasalubong money");

        // ── Scenario 3: Transfer to self ──────────────────────────────────
        ct.cashTransfer(1, 100.00, "Self transfer");

        // ── Scenario 4: Insufficient balance ─────────────────────────────
        ct.cashTransfer(2, 99999.00, "Too much");

        // ── Scenario 5: Exceeds max transfer limit ────────────────────────
        ct.cashTransfer(2, 60000.00, "Over the limit");

        // ── Scenario 6: Below minimum ─────────────────────────────────────
        ct.cashTransfer(2, 0.50, "Too small");

        // ── Scenario 7: Non-existent recipient ───────────────────────────
        ct.cashTransfer(99, 100.00, "Ghost user");

        // ── Scenario 8: Transfer without session ─────────────────────────
        CashTransfer ct2 = new CashTransfer(cb);   // no setSession()
        ct2.cashTransfer(2, 100.00, "No login");

        // ── Final balances ────────────────────────────────────────────────
        System.out.println("── Final Balances ──────────────────────────");
        cb.checkBalance(1);   // Juan:  5000 - 1000 - 500 = PHP 3,500.00
        cb.checkBalance(2);   // Maria: 12500.75 + 1000   = PHP 13,500.75
        cb.checkBalance(3);   // Pedro: 300.50 + 500       = PHP 800.50

        // ── Transaction histories ─────────────────────────────────────────
        ct.printTransactionHistory(1);
        ct.printTransactionHistory(2);
        ct.printTransactionHistory(3);

        System.out.println("======================================");
        System.out.println("           End of Demo                ");
        System.out.println("======================================");
    }
}