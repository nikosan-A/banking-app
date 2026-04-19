package com.gcashapp;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests – CheckBalance
 *
 * Verifies that checkBalance(userId) returns the exact amount
 * stored in the in-memory balance table (seeded dummy data).
 */
@DisplayName("CheckBalance Tests")
class CheckBalanceTest {

    private CheckBalance checkBalance;

    // Seeded dummy data (from CheckBalance constructor):
    //  user_id 1 → PHP 5,000.00
    //  user_id 2 → PHP 12,500.75
    //  user_id 3 → PHP 300.50
    //  user_id 4 → PHP 99,999.99

    @BeforeEach
    void setUp() {
        checkBalance = new CheckBalance();
    }

    // ─── BALANCE MATCHES DATABASE ─────────────────────────────────────────

    @Test
    @DisplayName("checkBalance – User 1 returns PHP 5000.00")
    void testBalanceUser1() {
        double balance = checkBalance.checkBalance(1);
        assertEquals(5000.00, balance, 0.001,
            "User 1 balance should match database value PHP 5000.00");
    }

    @Test
    @DisplayName("checkBalance – User 2 returns PHP 12500.75")
    void testBalanceUser2() {
        double balance = checkBalance.checkBalance(2);
        assertEquals(12500.75, balance, 0.001,
            "User 2 balance should match database value PHP 12500.75");
    }

    @Test
    @DisplayName("checkBalance – User 3 returns PHP 300.50")
    void testBalanceUser3() {
        double balance = checkBalance.checkBalance(3);
        assertEquals(300.50, balance, 0.001,
            "User 3 balance should match database value PHP 300.50");
    }

    @Test
    @DisplayName("checkBalance – User 4 returns PHP 99999.99")
    void testBalanceUser4() {
        double balance = checkBalance.checkBalance(4);
        assertEquals(99999.99, balance, 0.001,
            "User 4 balance should match database value PHP 99999.99");
    }

    // ─── EDGE CASES ───────────────────────────────────────────────────────

    @Test
    @DisplayName("checkBalance – non-existent user returns -1.0")
    void testBalanceUserNotFound() {
        double balance = checkBalance.checkBalance(99);
        assertEquals(-1.0, balance, 0.001,
            "Non-existent user should return -1.0");
    }

    @Test
    @DisplayName("checkBalance – invalid ID (0) returns -1.0")
    void testBalanceZeroId() {
        double balance = checkBalance.checkBalance(0);
        assertEquals(-1.0, balance, 0.001);
    }

    @Test
    @DisplayName("checkBalance – invalid ID (negative) returns -1.0")
    void testBalanceNegativeId() {
        double balance = checkBalance.checkBalance(-5);
        assertEquals(-1.0, balance, 0.001);
    }

    // ─── BALANCE REFLECTS UPDATES ─────────────────────────────────────────

    @Test
    @DisplayName("checkBalance – reflects manual balance update")
    void testBalanceAfterUpdate() {
        CheckBalance.Balance bal = checkBalance.findByUserId(1);
        assertNotNull(bal);
        bal.setAmount(8888.00);
        assertEquals(8888.00, checkBalance.checkBalance(1), 0.001,
            "checkBalance should return updated value after setAmount()");
    }

    // ─── addBalanceRecord ─────────────────────────────────────────────────

    @Test
    @DisplayName("addBalanceRecord – new user starts at PHP 0.00")
    void testAddBalanceRecord() {
        checkBalance.addBalanceRecord(10);
        double balance = checkBalance.checkBalance(10);
        assertEquals(0.00, balance, 0.001,
            "Newly added balance record should start at PHP 0.00");
    }
}
