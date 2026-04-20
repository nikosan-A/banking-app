package com.gcashapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * GcashApp – UserAuthentication
 *
 * Handles: register, login, changePin, logout.
 * Java 8 : Predicate validation, Optional lookup, stream duplicate check.
 *
 * Seeded demo accounts:
 *   ID 1 | Juan dela Cruz  | 09171234567 | PIN 123456
 *   ID 2 | Maria Santos    | 09189999999 | PIN 654321
 *   ID 3 | Pedro Reyes     | 09201234567 | PIN 111111
 *   ID 4 | Ana Lim         | 09270000001 | PIN 222222
 */
public class UserAuthentication {

    // ── Inner class: "users" table row ────────────────────────────────────
    public static class User {
        private final int    id;
        private final String name;
        private final String email;
        private final String number;
        private       String pin;

        public User(int id, String name, String email, String number, String pin) {
            this.id = id; this.name = name; this.email = email;
            this.number = number; this.pin = pin;
        }
        public int    getId()     { return id; }
        public String getName()   { return name; }
        public String getEmail()  { return email; }
        public String getNumber() { return number; }
        public String getPin()    { return pin; }
        public void   setPin(String pin) { this.pin = pin; }
    }

    // ── Java 8 Predicates (reused by tests directly) ──────────────────────
    public static final Predicate<String> VALID_NAME   =
            s -> s != null && !s.trim().isEmpty();

    public static final Predicate<String> VALID_EMAIL  =
            s -> s != null && s.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public static final Predicate<String> VALID_NUMBER =
            s -> s != null && s.matches("^09\\d{9}$");

    public static final Predicate<String> VALID_PIN    =
            s -> s != null && s.matches("^\\d{6}$");

    // ── State ─────────────────────────────────────────────────────────────
    private final List<User> usersTable   = new ArrayList<>();
    private       int        nextId       = 1;
    private       User       loggedInUser = null;

    public UserAuthentication() {
        usersTable.add(new User(nextId++, "Juan dela Cruz",  "juan@email.com",  "09171234567", "123456"));
        usersTable.add(new User(nextId++, "Maria Santos",    "maria@email.com", "09189999999", "654321"));
        usersTable.add(new User(nextId++, "Pedro Reyes",     "pedro@email.com", "09201234567", "111111"));
        usersTable.add(new User(nextId++, "Ana Lim",         "ana@email.com",   "09270000001", "222222"));
    }

    // ── register ──────────────────────────────────────────────────────────
    public int register(String name, String email, String number, String pin) {
        if (!VALID_NAME.test(name))    { System.out.println("[ERROR] Name cannot be empty.");                          return -1; }
        if (!VALID_EMAIL.test(email))  { System.out.println("[ERROR] Invalid email address.");                         return -1; }
        if (!VALID_NUMBER.test(number)){ System.out.println("[ERROR] Mobile must be 11 digits starting with '09'.");  return -1; }
        if (!VALID_PIN.test(pin))      { System.out.println("[ERROR] PIN must be exactly 6 numeric digits.");         return -1; }

        if (usersTable.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            System.out.println("[ERROR] Email already registered."); return -1; }
        if (usersTable.stream().anyMatch(u -> u.getNumber().equals(number))) {
            System.out.println("[ERROR] Mobile number already registered."); return -1; }

        User newUser = new User(nextId++, name.trim(), email.trim(), number.trim(), pin);
        usersTable.add(newUser);
        System.out.println("[SUCCESS] Registered! Welcome, " + newUser.getName() + ".");
        return newUser.getId();
    }

    // ── login ─────────────────────────────────────────────────────────────
    public int login(String number, String pin) {
        if (number == null || number.trim().isEmpty()) { System.out.println("[ERROR] Mobile number is required."); return -1; }
        if (pin    == null || pin.trim().isEmpty())    { System.out.println("[ERROR] PIN is required.");           return -1; }

        Optional<User> found = usersTable.stream()
                .filter(u -> u.getNumber().equals(number.trim()))
                .findFirst();

        if (!found.isPresent() || !found.get().getPin().equals(pin.trim())) {
            System.out.println("[ERROR] Invalid mobile number or PIN."); return -1; }

        loggedInUser = found.get();
        System.out.println("[SUCCESS] Welcome back, " + loggedInUser.getName() + "!");
        return loggedInUser.getId();
    }

    // ── changePin ─────────────────────────────────────────────────────────
    public boolean changePin(String oldPin, String newPin) {
        if (loggedInUser == null)                  { System.out.println("[ERROR] No active session.");                return false; }
        if (!loggedInUser.getPin().equals(oldPin)) { System.out.println("[ERROR] Old PIN is incorrect.");            return false; }
        if (!VALID_PIN.test(newPin))               { System.out.println("[ERROR] New PIN must be 6 numeric digits."); return false; }
        if (oldPin.equals(newPin))                 { System.out.println("[ERROR] New PIN must differ from old PIN."); return false; }
        loggedInUser.setPin(newPin);
        System.out.println("[SUCCESS] PIN changed successfully.");
        return true;
    }

    // ── logout ────────────────────────────────────────────────────────────
    public void logout() {
        if (loggedInUser == null) { System.out.println("[INFO] No active session."); return; }
        System.out.println("[SUCCESS] " + loggedInUser.getName() + " has been logged out.");
        loggedInUser = null;
    }

    public User       getCurrentUser() { return loggedInUser; }
    public List<User> getUsers()       { return usersTable; }
}
