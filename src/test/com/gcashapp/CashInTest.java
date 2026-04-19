package com.gcashapp

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests – CashIn
 *
 * Verifies that cashIn() correctly updates the user's balance
 * and rejects invalid inputs.
 */
@DisplayName("CashIn Tests")
class CashInTest {

    private CheckBalance checkBalance;
    private Transactions transactions;
    private CashIn       cashIn;

    @BeforeEach
    void setUp() {
        checkBalance = new CheckBalance();          // User 1 starts at PHP 5000.00
        transactions = new Transactions();
        cashIn       = new CashIn(checkBalance, transactions);
    }

    // ─── BALANCE UPDATES CORRECTLY ────────────────────────────────────────

    @Test
    @DisplayName("cashIn PHP 200 – balance increases by 200")
    void testCashInIncreasesBalance200() {
        double before = checkBalance.checkBalance(1);   // 5000.00
        cashIn.cashIn(1, 200.00, "Cash-in via 7-Eleven", 0);
        double after  = checkBalance.checkBalance(1);
        assertEquals(before + 200.00, after, 0.001,
            "Balance should increase by exactly PHP 200.00");
    }

    @Test
    @DisplayName("cashIn PHP 300 – balance increases by 300")
    void testCashInIncreasesBalance300() {
        double before = checkBalance.checkBalance(1);   // 5000.00
        cashIn.cashIn(1, 300.00, "Cash-in via Palawan Express", 0);
        double after  = checkBalance.checkBalance(1);
        assertEquals(before + 300.00, after, 0.001,
            "Balance should increase by exactly PHP 300.00");
    }

    @Test
    @DisplayName("cashIn – two sequential cash-ins accumulate correctly")
    void testTwoSequentialCashIns() {
        double before = checkBalance.checkBalance(1);   // 5000.00
        cashIn.cashIn(1, 200.00, "First",  0);
        cashIn.cashIn(1, 300.00, "Second", 0);
        double after  = checkBalance.checkBalance(1);
        assertEquals(before + 500.00, after, 0.001,
            "Two cash-ins of 200 + 300 should total PHP 5500.00");
    }

    @Test
    @DisplayName("cashIn – returns true on success")
    void testCashInReturnsTrue() {
        boolean result = cashIn.cashIn(1, 500.00, "BDO", 0);
        assertTrue(result, "Valid cash-in should return true");
    }

    // ─── TRANSACTION LOGGED ───────────────────────────────────────────────

    @Test
    @DisplayName("cashIn – transaction is recorded")
    void testCashInLogsTransaction() {
        cashIn.cashIn(1, 200.00, "Test cash-in", 0);
        assertEquals(1, transactions.viewUserAll(1).size(),
            "One transaction should be logged after one cash-in");
    }

    // ─── INVALID INPUTS ───────────────────────────────────────────────────

    @Test
    @DisplayName("cashIn – negative amount returns false")
    void testCashInNegativeAmount() {
        boolean result = cashIn.cashIn(1, -100.00, "Negative", 0);
        assertFalse(result);
    }

    @Test
    @DisplayName("cashIn – zero amount returns false")
    void testCashInZeroAmount() {
        boolean result = cashIn.cashIn(1, 0.00, "Zero", 0);
        assertFalse(result);
    }

    @Test
    @DisplayName("cashIn – amount below minimum (PHP 0.50) returns false")
    void testCashInBelowMinimum() {
        boolean result = cashIn.cashIn(1, 0.50, "Below min", 0);
        assertFalse(result);
    }

    @Test
    @DisplayName("cashIn – non-existent user returns false")
    void testCashInUnknownUser() {
        boolean result = cashIn.cashIn(99, 500.00, "Ghost", 0);
        assertFalse(result);
    }

    @Test
    @DisplayName("cashIn – invalid user ID (0) returns false")
    void testCashInInvalidId() {
        boolean result = cashIn.cashIn(0, 500.00, "Invalid", 0);
        assertFalse(result);
    }

    @Test
    @DisplayName("cashIn – balance unchanged on failed cash-in")
    void testCashInBalanceUnchangedOnFailure() {
        double before = checkBalance.checkBalance(1);
        cashIn.cashIn(1, -999.00, "Should fail", 0);
        double after = checkBalance.checkBalance(1);
        assertEquals(before, after, 0.001,
            "Balance should not change when cash-in fails");
    }
}
