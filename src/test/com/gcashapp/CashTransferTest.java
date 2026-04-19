package com.gcashapp;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests – CashTransfer
 *
 * Verifies that cashTransfer() correctly updates BOTH the sender's
 * and recipient's balance, and rejects all restricted scenarios.
 */
@DisplayName("CashTransfer Tests")
class CashTransferTest {

    private CheckBalance checkBalance;
    private Transactions transactions;
    private CashTransfer cashTransfer;

    // Seeded balances:
    //  User 1 (Juan)  → PHP 5,000.00
    //  User 2 (Maria) → PHP 12,500.75
    //  User 3 (Pedro) → PHP 300.50

    @BeforeEach
    void setUp() {
        checkBalance = new CheckBalance();
        transactions = new Transactions();
        cashTransfer = new CashTransfer(checkBalance, transactions);
    }

    // ─── BOTH BALANCES UPDATE CORRECTLY ──────────────────────────────────

    @Test
    @DisplayName("cashTransfer – sender balance decreases by transfer amount")
    void testSenderBalanceDecreases() {
        double senderBefore = checkBalance.checkBalance(1);   // 5000.00
        cashTransfer.cashTransfer(1, 2, 1000.00, "Test");
        double senderAfter  = checkBalance.checkBalance(1);
        assertEquals(senderBefore - 1000.00, senderAfter, 0.001,
            "Sender balance should decrease by PHP 1000.00");
    }

    @Test
    @DisplayName("cashTransfer – recipient balance increases by transfer amount")
    void testRecipientBalanceIncreases() {
        double recipientBefore = checkBalance.checkBalance(2);   // 12500.75
        cashTransfer.cashTransfer(1, 2, 1000.00, "Test");
        double recipientAfter  = checkBalance.checkBalance(2);
        assertEquals(recipientBefore + 1000.00, recipientAfter, 0.001,
            "Recipient balance should increase by PHP 1000.00");
    }

    @Test
    @DisplayName("cashTransfer – total money in system is conserved")
    void testTotalMoneyConserved() {
        double totalBefore = checkBalance.checkBalance(1) + checkBalance.checkBalance(2);
        cashTransfer.cashTransfer(1, 2, 500.00, "Conservation test");
        double totalAfter  = checkBalance.checkBalance(1) + checkBalance.checkBalance(2);
        assertEquals(totalBefore, totalAfter, 0.001,
            "Total money across both accounts should not change");
    }

    @Test
    @DisplayName("cashTransfer – returns true on valid transfer")
    void testCashTransferReturnsTrue() {
        boolean result = cashTransfer.cashTransfer(1, 2, 100.00, "Valid");
        assertTrue(result);
    }

    // ─── TWO TRANSACTIONS LOGGED ──────────────────────────────────────────

    @Test
    @DisplayName("cashTransfer – logs one SENT and one RECEIVED transaction")
    void testTwoTransactionsLogged() {
        cashTransfer.cashTransfer(1, 2, 500.00, "Two log test");
        int senderTxns    = transactions.viewUserAll(1).size();
        int recipientTxns = transactions.viewUserAll(2).size();
        assertEquals(1, senderTxns,    "Sender should have 1 transaction (SENT)");
        assertEquals(1, recipientTxns, "Recipient should have 1 transaction (RECEIVED)");
    }

    // ─── RESTRICTIONS ────────────────────────────────────────────────────

    @Test
    @DisplayName("cashTransfer – transfer to self returns false")
    void testTransferToSelf() {
        boolean result = cashTransfer.cashTransfer(1, 1, 100.00, "Self");
        assertFalse(result, "Should not be able to transfer to yourself");
    }

    @Test
    @DisplayName("cashTransfer – insufficient balance returns false")
    void testInsufficientBalance() {
        boolean result = cashTransfer.cashTransfer(1, 2, 99999.00, "Too much");
        assertFalse(result, "Should reject transfer exceeding available balance");
    }

    @Test
    @DisplayName("cashTransfer – insufficient balance leaves balances unchanged")
    void testInsufficientBalanceLeavesBalancesUnchanged() {
        double s = checkBalance.checkBalance(1);
        double r = checkBalance.checkBalance(2);
        cashTransfer.cashTransfer(1, 2, 99999.00, "Should fail");
        assertEquals(s, checkBalance.checkBalance(1), 0.001);
        assertEquals(r, checkBalance.checkBalance(2), 0.001);
    }

    @Test
    @DisplayName("cashTransfer – exceeds max limit (PHP 50,000) returns false")
    void testExceedsMaxLimit() {
        boolean result = cashTransfer.cashTransfer(4, 2, 60000.00, "Over limit");
        assertFalse(result);
    }

    @Test
    @DisplayName("cashTransfer – below minimum (PHP 0.50) returns false")
    void testBelowMinimum() {
        boolean result = cashTransfer.cashTransfer(1, 2, 0.50, "Too small");
        assertFalse(result);
    }

    @Test
    @DisplayName("cashTransfer – non-existent recipient returns false")
    void testNonExistentRecipient() {
        boolean result = cashTransfer.cashTransfer(1, 99, 100.00, "Ghost");
        assertFalse(result);
    }

    @Test
    @DisplayName("cashTransfer – invalid sender ID returns false")
    void testInvalidSenderId() {
        boolean result = cashTransfer.cashTransfer(-1, 2, 100.00, "Invalid sender");
        assertFalse(result);
    }

    @Test
    @DisplayName("cashTransfer – note over 50 chars returns false")
    void testNoteTooLong() {
        String longNote = "A".repeat(51);
        boolean result = cashTransfer.cashTransfer(1, 2, 100.00, longNote);
        assertFalse(result);
    }
}
