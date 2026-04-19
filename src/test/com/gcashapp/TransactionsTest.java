package com.gcashapp;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests – Transactions
 *
 * Verifies that viewAll(), viewUserAll(), and viewTransaction()
 * display and return transaction data correctly.
 */
@DisplayName("Transactions Tests")
class TransactionsTest {

    private CheckBalance checkBalance;
    private Transactions transactions;
    private CashIn       cashIn;
    private CashTransfer cashTransfer;

    @BeforeEach
    void setUp() {
        checkBalance = new CheckBalance();
        transactions = new Transactions();
        cashIn       = new CashIn(checkBalance, transactions);
        cashTransfer = new CashTransfer(checkBalance, transactions);
    }

    // ─── viewAll ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("viewAll – empty table returns empty list")
    void testViewAllEmpty() {
        List<Transactions.Transaction> all = transactions.viewAll();
        assertTrue(all.isEmpty(), "viewAll on empty table should return empty list");
    }

    @Test
    @DisplayName("viewAll – returns all logged transactions")
    void testViewAllReturnsCorrectCount() {
        cashIn.cashIn(1, 200.00, "Cash-in 1", 0);   // 1 txn
        cashIn.cashIn(2, 500.00, "Cash-in 2", 0);   // 1 txn
        cashTransfer.cashTransfer(1, 2, 100.00, "Transfer"); // 2 txns
        List<Transactions.Transaction> all = transactions.viewAll();
        assertEquals(4, all.size(),
            "viewAll should return all 4 transaction records");
    }

    @Test
    @DisplayName("viewAll – transaction amounts are correct")
    void testViewAllAmounts() {
        cashIn.cashIn(1, 777.00, "Lucky cash-in", 0);
        List<Transactions.Transaction> all = transactions.viewAll();
        assertEquals(777.00, all.get(0).getAmount(), 0.001,
            "Transaction amount should match what was cashed in");
    }

    // ─── viewUserAll ─────────────────────────────────────────────────────

    @Test
    @DisplayName("viewUserAll – returns only transactions for that user")
    void testViewUserAllFiltersCorrectly() {
        cashIn.cashIn(1, 200.00, "Juan cash-in",  0);
        cashIn.cashIn(2, 500.00, "Maria cash-in", 0);
        cashIn.cashIn(1, 300.00, "Juan cash-in 2", 0);

        List<Transactions.Transaction> juanTxns = transactions.viewUserAll(1);
        assertEquals(2, juanTxns.size(),
            "viewUserAll(1) should return exactly 2 records for Juan");
    }

    @Test
    @DisplayName("viewUserAll – all returned records belong to the queried user")
    void testViewUserAllAllBelongToUser() {
        cashIn.cashIn(1, 100.00, "A", 0);
        cashIn.cashIn(2, 200.00, "B", 0);
        cashIn.cashIn(1, 300.00, "C", 0);

        List<Transactions.Transaction> txns = transactions.viewUserAll(1);
        for (Transactions.Transaction t : txns) {
            assertEquals(1, t.getAccountId(),
                "Every returned transaction should belong to User 1");
        }
    }

    @Test
    @DisplayName("viewUserAll – user with no transactions returns empty list")
    void testViewUserAllNoTransactions() {
        List<Transactions.Transaction> result = transactions.viewUserAll(3);
        assertTrue(result.isEmpty(),
            "User 3 (no transactions) should return empty list");
    }

    @Test
    @DisplayName("viewUserAll – invalid user ID returns empty list")
    void testViewUserAllInvalidId() {
        List<Transactions.Transaction> result = transactions.viewUserAll(-1);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("viewUserAll – transfer logs SENT on sender's record")
    void testViewUserAllSentRecord() {
        cashTransfer.cashTransfer(1, 2, 500.00, "Bayad");
        List<Transactions.Transaction> juanTxns = transactions.viewUserAll(1);
        assertEquals(1, juanTxns.size());
        assertTrue(juanTxns.get(0).getName().startsWith("SENT"),
            "Sender's transaction name should start with 'SENT'");
    }

    @Test
    @DisplayName("viewUserAll – transfer logs RECEIVED on recipient's record")
    void testViewUserAllReceivedRecord() {
        cashTransfer.cashTransfer(1, 2, 500.00, "Bayad");
        List<Transactions.Transaction> mariaTxns = transactions.viewUserAll(2);
        assertEquals(1, mariaTxns.size());
        assertTrue(mariaTxns.get(0).getName().startsWith("RECEIVED"),
            "Recipient's transaction name should start with 'RECEIVED'");
    }

    // ─── viewTransaction ─────────────────────────────────────────────────

    @Test
    @DisplayName("viewTransaction – returns correct transaction by ID")
    void testViewTransactionById() {
        cashIn.cashIn(1, 250.00, "Specific txn", 0);
        Transactions.Transaction txn = transactions.viewTransaction(1);
        assertNotNull(txn, "Transaction #1 should exist");
        assertEquals(250.00, txn.getAmount(), 0.001,
            "Amount of transaction #1 should be PHP 250.00");
    }

    @Test
    @DisplayName("viewTransaction – returns correct description")
    void testViewTransactionDescription() {
        cashIn.cashIn(1, 100.00, "My Label", 0);
        Transactions.Transaction txn = transactions.viewTransaction(1);
        assertNotNull(txn);
        assertEquals("My Label", txn.getName());
    }

    @Test
    @DisplayName("viewTransaction – non-existent ID returns null")
    void testViewTransactionNotFound() {
        Transactions.Transaction txn = transactions.viewTransaction(99);
        assertNull(txn, "Non-existent transaction ID should return null");
    }

    @Test
    @DisplayName("viewTransaction – invalid ID (0) returns null")
    void testViewTransactionZeroId() {
        Transactions.Transaction txn = transactions.viewTransaction(0);
        assertNull(txn);
    }

    @Test
    @DisplayName("viewTransaction – invalid ID (negative) returns null")
    void testViewTransactionNegativeId() {
        Transactions.Transaction txn = transactions.viewTransaction(-3);
        assertNull(txn);
    }

    @Test
    @DisplayName("viewTransaction – second transaction has ID 2")
    void testViewTransactionSecondRecord() {
        cashIn.cashIn(1, 100.00, "First",  0);
        cashIn.cashIn(2, 200.00, "Second", 0);
        Transactions.Transaction txn = transactions.viewTransaction(2);
        assertNotNull(txn);
        assertEquals(200.00, txn.getAmount(), 0.001);
    }
}
