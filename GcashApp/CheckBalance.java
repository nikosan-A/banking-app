import java.sql.*;

/**
 * GcashApp – CheckBalance
 *
 * Models the "Balance" table and provides a checkBalance() method
 * that returns a user's balance using their userID as a parameter.
 *
 * Database used : SQLite (embedded – no server setup needed)
 * JDBC API      : Java SE 8 java.sql.*
 *
 * Table schema
 * ─────────────────────────────────────────
 *  users   : id | name | email | number | pin
 *  balance : id | amount | user_id
 * ─────────────────────────────────────────
 */
public class CheckBalance {

    // ── JDBC connection string (SQLite file-based DB) ──────────────────────
    // Change this path to match your local project folder.
    // For MySQL  → "jdbc:mysql://localhost:3306/gcashapp"
    // For SQLite → "jdbc:sqlite:gcashapp.db"
    private static final String DB_URL  = "jdbc:sqlite:gcashapp.db";

    // ── Obtain a connection ────────────────────────────────────────────────
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ─────────────────────────────────────────────────────────────────────
    // DATABASE SETUP  (creates tables + seeds dummy data if not yet present)
    // ─────────────────────────────────────────────────────────────────────
    public void initDatabase() {
        String createUsers =
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id      INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name    TEXT    NOT NULL," +
            "  email   TEXT    NOT NULL UNIQUE," +
            "  number  TEXT    NOT NULL UNIQUE," +
            "  pin     TEXT    NOT NULL" +
            ");";

        String createBalance =
            "CREATE TABLE IF NOT EXISTS balance (" +
            "  id      INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  amount  REAL    NOT NULL DEFAULT 0.00," +
            "  user_id INTEGER NOT NULL," +
            "  FOREIGN KEY (user_id) REFERENCES users(id)" +
            ");";

        // ── Dummy / seed data ──────────────────────────────────────────────
        String seedUsers =
            "INSERT OR IGNORE INTO users (id, name, email, number, pin) VALUES" +
            "  (1, 'Juan dela Cruz',  'juan@email.com',  '09171234567', '123456')," +
            "  (2, 'Maria Santos',    'maria@email.com', '09189999999', '654321')," +
            "  (3, 'Pedro Reyes',     'pedro@email.com', '09201234567', '111111')," +
            "  (4, 'Ana Lim',         'ana@email.com',   '09270000001', '222222');";

        String seedBalance =
            "INSERT OR IGNORE INTO balance (id, amount, user_id) VALUES" +
            "  (1, 5000.00,  1)," +   // Juan
            "  (2, 12500.75, 2)," +   // Maria
            "  (3, 300.50,   3)," +   // Pedro
            "  (4, 99999.99, 4);";    // Ana

        try (Connection conn = getConnection();
             Statement  stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createBalance);
            stmt.execute(seedUsers);
            stmt.execute(seedBalance);
            System.out.println("[DB] Tables created and dummy data seeded successfully.\n");

        } catch (SQLException e) {
            System.out.println("[DB ERROR] initDatabase: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // checkBalance  – returns balance amount for the given userID
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the current balance of the user identified by {@code userId}.
     *
     * @param  userId  the ID returned by UserAuthentication.login()
     * @return the balance amount, or -1.0 if the user / balance is not found
     */
    public double checkBalance(int userId) {

        System.out.println("── CHECK BALANCE ──────────────────────────");

        // ── Validate input ────────────────────────────────────────────────
        if (userId <= 0) {
            System.out.println("[ERROR] Invalid user ID: " + userId);
            return -1.0;
        }

        // ── JDBC query ────────────────────────────────────────────────────
        String sql =
            "SELECT b.amount, u.name " +
            "FROM   balance b " +
            "JOIN   users   u ON u.id = b.user_id " +
            "WHERE  b.user_id = ?";

        try (Connection        conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    double balance = rs.getDouble("amount");
                    String name    = rs.getString("name");

                    System.out.printf("[SUCCESS] Account holder : %s%n", name);
                    System.out.printf("          Available Balance: PHP %.2f%n%n", balance);
                    return balance;

                } else {
                    System.out.println("[ERROR] No balance record found for user ID: " + userId);
                    return -1.0;
                }
            }

        } catch (SQLException e) {
            System.out.println("[DB ERROR] checkBalance: " + e.getMessage());
            return -1.0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // MAIN – Demo / Test scenarios
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        CheckBalance cb = new CheckBalance();

        System.out.println("======================================");
        System.out.println("   GcashApp – Check Balance Module    ");
        System.out.println("======================================\n");

        // Step 1: Set up DB (tables + seed data)
        cb.initDatabase();

        // Step 2: Check balance for existing users
        cb.checkBalance(1);   // Juan   → PHP 5,000.00
        cb.checkBalance(2);   // Maria  → PHP 12,500.75
        cb.checkBalance(3);   // Pedro  → PHP 300.50
        cb.checkBalance(4);   // Ana    → PHP 99,999.99

        // Step 3: Edge cases
        cb.checkBalance(99);  // Non-existent user
        cb.checkBalance(-1);  // Invalid ID

        System.out.println("======================================");
        System.out.println("           End of Demo                ");
        System.out.println("======================================");
    }
}