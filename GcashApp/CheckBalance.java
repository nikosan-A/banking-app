import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp – CheckBalance
 *
 * Models the "balance" table using an in-memory List.
 * No external JDBC driver required — works out of the box.
 */
public class CheckBalance {

    // ── Inner class: "balance" table row ──────────────────────────────────
    static class Balance {
        private int    id;
        private double amount;
        private int    userId;

        public Balance(int id, double amount, int userId) {
            this.id     = id;
            this.amount = amount;
            this.userId = userId;
        }

        public int    getId()     { return id; }
        public double getAmount() { return amount; }
        public int    getUserId() { return userId; }
        public void   setAmount(double amount) { this.amount = amount; }
    }

    // ── In-memory "balance" table ─────────────────────────────────────────
    List<Balance> balanceTable = new ArrayList<>();

    // ── Constructor: seeds dummy data ─────────────────────────────────────
    public CheckBalance() {
        // Temporary dummy data (mirrors the users from UserAuthentication)
        balanceTable.add(new Balance(1,  5000.00,  1));  // Juan
        balanceTable.add(new Balance(2, 12500.75,  2));  // Maria
        balanceTable.add(new Balance(3,   300.50,  3));  // Pedro
        balanceTable.add(new Balance(4, 99999.99,  4));  // Ana
        System.out.println("[DB] Balance table initialized with dummy data.\n");
    }

    // ─────────────────────────────────────────────────────────────────────
    // checkBalance – returns the balance amount for a given userID
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the current balance for the given {@code userId}.
     *
     * @param  userId  the ID returned by UserAuthentication.login()
     * @return balance amount, or -1.0 if not found
     */
    public double checkBalance(int userId) {
        System.out.println("── CHECK BALANCE ──────────────────────────");

        if (userId <= 0) {
            System.out.println("[ERROR] Invalid user ID: " + userId);
            return -1.0;
        }

        for (Balance b : balanceTable) {
            if (b.getUserId() == userId) {
                System.out.printf("[SUCCESS] User ID          : %d%n",    userId);
                System.out.printf("          Available Balance : PHP %.2f%n%n", b.getAmount());
                return b.getAmount();
            }
        }

        System.out.println("[ERROR] No balance record found for user ID: " + userId + "\n");
        return -1.0;
    }

    // ── Internal helper used by CashIn to update a balance row ───────────
    Balance findByUserId(int userId) {
        for (Balance b : balanceTable) {
            if (b.getUserId() == userId) return b;
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────
    // MAIN – Demo
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("   GcashApp – Check Balance Module    ");
        System.out.println("======================================\n");

        CheckBalance cb = new CheckBalance();

        cb.checkBalance(1);   // Juan   → PHP 5,000.00
        cb.checkBalance(2);   // Maria  → PHP 12,500.75
        cb.checkBalance(3);   // Pedro  → PHP 300.50
        cb.checkBalance(4);   // Ana    → PHP 99,999.99
        cb.checkBalance(99);  // Not found
        cb.checkBalance(-1);  // Invalid ID

        System.out.println("======================================");
        System.out.println("           End of Demo                ");
        System.out.println("======================================");
    }
}