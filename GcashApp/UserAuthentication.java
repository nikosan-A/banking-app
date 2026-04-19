import java.util.ArrayList;
import java.util.List;

/**
 * GcashApp - UserAuthentication
 * Simulates a GCash-like online banking user authentication system.
 * Uses an in-memory "database" (List) to store user records.
 */
public class UserAuthentication {

    // ─────────────────────────────────────────────
    // Inner class representing the "users" table
    // ─────────────────────────────────────────────
    static class User {
        private int id;
        private String name;
        private String email;
        private String number;   // mobile number
        private String pin;

        public User(int id, String name, String email, String number, String pin) {
            this.id     = id;
            this.name   = name;
            this.email  = email;
            this.number = number;
            this.pin    = pin;
        }

        public int    getId()     { return id; }
        public String getName()   { return name; }
        public String getEmail()  { return email; }
        public String getNumber() { return number; }
        public String getPin()    { return pin; }
        public void   setPin(String pin) { this.pin = pin; }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email +
                   "', number='" + number + "'}";
        }
    }

    // ─────────────────────────────────────────────
    // In-memory database & session state
    // ─────────────────────────────────────────────
    private List<User> usersTable = new ArrayList<>();
    private int        nextId     = 1;
    private User       loggedInUser = null;   // tracks current session

    // ─────────────────────────────────────────────
    // 1. REGISTRATION
    // ─────────────────────────────────────────────
    /**
     * Registers a new user after validating every field.
     * @return the new user's ID, or -1 if registration failed.
     */
    public int register(String name, String email, String number, String pin) {

        System.out.println("\n── REGISTRATION ──────────────────────────");

        // ── Validation ──────────────────────────
        if (name == null || name.trim().isEmpty()) {
            System.out.println("[ERROR] Name cannot be empty.");
            return -1;
        }

        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("[ERROR] Invalid email address: " + email);
            return -1;
        }

        // Philippine mobile: starts with 09, exactly 11 digits
        if (number == null || !number.matches("^09\\d{9}$")) {
            System.out.println("[ERROR] Mobile number must be 11 digits starting with '09'.");
            return -1;
        }

        // PIN: exactly 6 digits
        if (pin == null || !pin.matches("^\\d{6}$")) {
            System.out.println("[ERROR] PIN must be exactly 6 numeric digits.");
            return -1;
        }

        // Duplicate checks
        for (User u : usersTable) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                System.out.println("[ERROR] Email already registered.");
                return -1;
            }
            if (u.getNumber().equals(number)) {
                System.out.println("[ERROR] Mobile number already registered.");
                return -1;
            }
        }

        // ── Save user ──────────────────────────
        User newUser = new User(nextId++, name.trim(), email.trim(), number.trim(), pin);
        usersTable.add(newUser);
        System.out.println("[SUCCESS] User registered! ID = " + newUser.getId() +
                           " | Name: " + newUser.getName());
        return newUser.getId();
    }

    // ─────────────────────────────────────────────
    // 2. LOGIN
    // ─────────────────────────────────────────────
    /**
     * Authenticates a user by mobile number + PIN.
     * Handles common error scenarios securely.
     * @return the authenticated user's ID, or -1 on failure.
     */
    public int login(String number, String pin) {

        System.out.println("\n── LOGIN ──────────────────────────────────");

        // ── Input validation ──────────────────
        if (number == null || number.trim().isEmpty()) {
            System.out.println("[ERROR] Mobile number is required.");
            return -1;
        }
        if (pin == null || pin.trim().isEmpty()) {
            System.out.println("[ERROR] PIN is required.");
            return -1;
        }

        // ── Look up user by number ─────────────
        User found = null;
        for (User u : usersTable) {
            if (u.getNumber().equals(number.trim())) {
                found = u;
                break;
            }
        }

        // ── User-not-found scenario ────────────
        if (found == null) {
            // Generic message — do NOT reveal whether the number exists
            System.out.println("[ERROR] Invalid mobile number or PIN. Please try again.");
            return -1;
        }

        // ── Wrong PIN scenario ─────────────────
        if (!found.getPin().equals(pin.trim())) {
            System.out.println("[ERROR] Invalid mobile number or PIN. Please try again.");
            return -1;
        }

        // ── Success ───────────────────────────
        loggedInUser = found;
        System.out.println("[SUCCESS] Welcome, " + found.getName() + "! (ID: " + found.getId() + ")");
        return found.getId();
    }

    // ─────────────────────────────────────────────
    // 3. CHANGE PIN
    // ─────────────────────────────────────────────
    /**
     * Allows the currently logged-in user to change their PIN.
     * Requires the old PIN for verification.
     */
    public boolean changePin(String oldPin, String newPin) {

        System.out.println("\n── CHANGE PIN ─────────────────────────────");

        if (loggedInUser == null) {
            System.out.println("[ERROR] No active session. Please log in first.");
            return false;
        }

        if (oldPin == null || !loggedInUser.getPin().equals(oldPin.trim())) {
            System.out.println("[ERROR] Old PIN is incorrect.");
            return false;
        }

        if (newPin == null || !newPin.matches("^\\d{6}$")) {
            System.out.println("[ERROR] New PIN must be exactly 6 numeric digits.");
            return false;
        }

        if (oldPin.trim().equals(newPin.trim())) {
            System.out.println("[ERROR] New PIN must be different from the old PIN.");
            return false;
        }

        loggedInUser.setPin(newPin.trim());
        System.out.println("[SUCCESS] PIN changed successfully for " + loggedInUser.getName() + ".");
        return true;
    }

    // ─────────────────────────────────────────────
    // 4. LOGOUT
    // ─────────────────────────────────────────────
    /**
     * Ends the current user session.
     */
    public void logout() {

        System.out.println("\n── LOGOUT ─────────────────────────────────");

        if (loggedInUser == null) {
            System.out.println("[INFO] No active session to log out from.");
            return;
        }

        System.out.println("[SUCCESS] " + loggedInUser.getName() + " has been logged out.");
        loggedInUser = null;
    }

    // ─────────────────────────────────────────────
    // Utility: get current session user
    // ─────────────────────────────────────────────
    public User getCurrentUser() {
        return loggedInUser;
    }

    // ─────────────────────────────────────────────
    // MAIN — Demo / Test scenarios
    // ─────────────────────────────────────────────
    public static void main(String[] args) {

        UserAuthentication auth = new UserAuthentication();

        System.out.println("======================================");
        System.out.println("   GcashApp – User Authentication     ");
        System.out.println("======================================");

        // ── Scenario 1: Valid registration ────
        auth.register("Juan dela Cruz", "juan@email.com", "09171234567", "123456");

        // ── Scenario 2: Duplicate email ───────
        auth.register("Maria Santos", "juan@email.com", "09189999999", "654321");

        // ── Scenario 3: Invalid PIN format ────
        auth.register("Pedro Reyes", "pedro@email.com", "09201234567", "12ab");

        // ── Scenario 4: Valid second user ─────
        auth.register("Maria Santos", "maria@email.com", "09189999999", "654321");

        // ── Scenario 5: Wrong PIN login ───────
        auth.login("09171234567", "000000");

        // ── Scenario 6: Non-existent number ──
        auth.login("09000000000", "123456");

        // ── Scenario 7: Successful login ──────
        int userId = auth.login("09171234567", "123456");
        System.out.println("   → Returned user ID: " + userId);

        // ── Scenario 8: Change PIN ────────────
        auth.changePin("123456", "999999");

        // ── Scenario 9: Login with new PIN ────
        auth.logout();
        auth.login("09171234567", "999999");

        // ── Scenario 10: Change PIN while logged out ──
        auth.logout();
        auth.changePin("999999", "111111");

        System.out.println("\n======================================");
        System.out.println("           End of Demo                ");
        System.out.println("======================================");
    }
}