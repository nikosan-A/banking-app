package com.gcashapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TransactionsTest
 * Extends BaseTest (Inheritance).
 *
 * Java 8 used:
 *   Polymorphism – BankingOperation[] to seed diverse transaction types
 *   Lambda       – assertAll(), forEach(), countWhere(), filterList()
 *   Predicate    – byUser(), isCashIn(), isSent(), isReceived(),
 *                  amountGreaterThan(), and composed predicates (.and(), .or())
 */
@DisplayName("Transactions Tests")
class TransactionsTest extends BaseTest {

    /** Seeds a standard set of transactions used by multiple tests. */
    private void seedTransactions() {
        cashIn.cashIn(1, 200.00,  "Cash-in 7-Eleven",        0);
        cashIn.cashIn(1, 300.00,  "Cash-in Palawan Express",  0);
        cashIn.cashIn(2, 1500.00, "Cash-in BDO",              0);
        cashTransfer.cashTransfer(1, 2, 1000.00, "Bayad sa utang");
        cashTransfer.cashTransfer(2, 3,  500.00, "Pasalubong");
        // Results: 7 records (3 cash-ins + 2×SENT + 2×RECEIVED)
    }

    // ─── viewAll ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("viewAll – empty table returns empty list")
    void testViewAllEmpty() {
        assertTrue(transactions.viewAll().isEmpty(), "Empty table must return empty list");
    }

    @Test
    @DisplayName("viewAll – returns correct total record count after seeding")
    void testViewAllTotalCount() {
        seedTransactions();
        assertEquals(7, transactions.viewAll().size(), "Should have 7 total records");
    }

    @Test
    @DisplayName("viewAll – every returned amount is positive (Predicate check)")
    void testViewAllAmountsArePositive() {
        seedTransactions();
        long badCount = countWhere(transactions.viewAll(), t -> t.getAmount() <= 0);
        assertEquals(0L, badCount, "No transaction should have a non-positive amount");
    }

    // ─── POLYMORPHISM IN SEEDING ──────────────────────────────────────────

    @Test
    @DisplayName("viewAll – BankingOperation[] seeds transactions polymorphically")
    void testPolymorphicSeedingLogsCorrectly() {
        BankingOperation[] ops = {
            cashIn.forUser(1).amount(100.00).description("Poly1").from(0),
            cashIn.forUser(2).amount(200.00).description("Poly2").from(0),
            cashTransfer.from(1).to(3).amount(50.00).note("Poly3")
        };
        for (BankingOperation op : ops) executeOperation(op);

        // 2 cash-ins + 2 transfer records = 4
        assertEquals(4, transactions.viewAll().size(),
                     "Polymorphic ops should produce 4 transaction records");
    }

    // ─── viewUserAll ─────────────────────────────────────────────────────

    @Test
    @DisplayName("viewUserAll – returns only records for the queried user")
    void testViewUserAllFiltersCorrectly() {
        seedTransactions();
        List<Transactions.Transaction> juanTxns = transactions.viewUserAll(1);

        long wrongOwner = countWhere(juanTxns, t -> t.getAccountId() != 1);
        assertEquals(0L, wrongOwner, "All returned records must belong to User 1");
    }

    @Test
    @DisplayName("viewUserAll – correct counts per user")
    void testViewUserAllCountsPerUser() {
        seedTransactions();
        assertAll("per-user transaction counts",
            () -> assertEquals(3, transactions.viewUserAll(1).size(), "Juan:  2 cash-in + 1 SENT"),
            () -> assertEquals(3, transactions.viewUserAll(2).size(), "Maria: 1 cash-in + 1 RECEIVED + 1 SENT"),
            () -> assertEquals(1, transactions.viewUserAll(3).size(), "Pedro: 1 RECEIVED")
        );
    }

    @Test
    @DisplayName("viewUserAll – Juan's transactions are all CASH-IN or SENT (composed Predicate)")
    void testJuanHasOnlyCashInOrSent() {
        seedTransactions();
        List<Transactions.Transaction> juanTxns = transactions.viewUserAll(1);

        // .or() Predicate composition
        Predicate<Transactions.Transaction> cashInOrSent =
                Transactions.isCashIn().or(Transactions.isSent());

        long matched = countWhere(juanTxns, cashInOrSent);
        assertEquals((long) juanTxns.size(), matched,
                     "All of Juan's records should be CASH-IN or SENT");
    }

    @Test
    @DisplayName("viewUserAll – invalid IDs return empty list")
    void testViewUserAllInvalidIds() {
        assertAll("invalid IDs",
            () -> assertTrue(transactions.viewUserAll(-1).isEmpty(), "ID -1"),
            () -> assertTrue(transactions.viewUserAll(0).isEmpty(),  "ID 0")
        );
    }

    // ─── viewTransaction ─────────────────────────────────────────────────

    @Test
    @DisplayName("viewTransaction – returns transaction with correct data")
    void testViewTransactionCorrectData() {
        cashIn.cashIn(1, 250.00, "Specific txn", 0);
        Transactions.Transaction txn = transactions.viewTransaction(1);

        assertAll("transaction data",
            () -> assertNotNull(txn,                              "Transaction #1 must exist"),
            () -> assertEquals(1, txn.getId(),                   "ID = 1"),
            () -> assertWith(txn.getAmount(), equalsAmount(250.00), "Amount = PHP 250.00"),
            () -> assertEquals("Specific txn", txn.getName(),   "Name matches"),
            () -> assertEquals(1, txn.getAccountId(),            "AccountId = User 1")
        );
    }

    @Test
    @DisplayName("viewTransaction – invalid / non-existent IDs return null")
    void testViewTransactionInvalidIds() {
        assertAll("invalid transaction IDs",
            () -> assertNull(transactions.viewTransaction(0),   "ID 0 → null"),
            () -> assertNull(transactions.viewTransaction(-3),  "ID -3 → null"),
            () -> assertNull(transactions.viewTransaction(99),  "ID 99 → null (not found)")
        );
    }

    // ─── query() WITH CUSTOM PREDICATE ────────────────────────────────────

    @Test
    @DisplayName("query – amountGreaterThan(999) filters only large transactions")
    void testQueryAmountGreaterThan() {
        seedTransactions();
        List<Transactions.Transaction> bigTxns =
                transactions.query(Transactions.amountGreaterThan(999.00));

        assertFalse(bigTxns.isEmpty(), "There should be transactions over PHP 999");
        // Lambda: every result must satisfy the predicate
        bigTxns.forEach(t ->
            assertTrue(t.getAmount() > 999.00,
                       "Each result must have amount > PHP 999: got " + t.getAmount())
        );
    }

    @Test
    @DisplayName("query – composed Predicate: User 2 AND amount > 999")
    void testQueryComposedPredicate() {
        seedTransactions();
        Predicate<Transactions.Transaction> user2BigTxn =
                Transactions.byUser(2).and(Transactions.amountGreaterThan(999.00));

        List<Transactions.Transaction> results = transactions.query(user2BigTxn);
        assertFalse(results.isEmpty(), "User 2 should have at least one transaction over PHP 999");

        results.forEach(t -> assertAll("composed predicate per record",
            () -> assertEquals(2, t.getAccountId(), "Must belong to User 2"),
            () -> assertTrue(t.getAmount() > 999,   "Amount must exceed PHP 999")
        ));
    }

    @Test
    @DisplayName("query – Predicate.negate() returns records that do NOT match")
    void testQueryNegatedPredicate() {
        seedTransactions();
        Predicate<Transactions.Transaction> isCashIn  = Transactions.isCashIn();
        List<Transactions.Transaction> nonCashIn = transactions.query(isCashIn.negate());

        // Every result must NOT be a cash-in (i.e. must be SENT or RECEIVED)
        nonCashIn.forEach(t ->
            assertFalse(t.getTransferFromId() == 0,
                        "Negated cash-in predicate must exclude external cash-ins")
        );
    }
}
