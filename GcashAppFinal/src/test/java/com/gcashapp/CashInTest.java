package com.gcashapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CashInTest
 * Extends BaseTest (Inheritance).
 *
 * Java 8 used:
 *   Polymorphism – CashIn used as BankingOperation via executeOperation()
 *   Lambda       – assertAll(), assertOperation() with Supplier
 *   Predicate    – CashIn.VALID_AMOUNT, equalsAmount(), countWhere()
 */
@DisplayName("CashIn Tests")
class CashInTest extends BaseTest {

    // ─── POLYMORPHISM ─────────────────────────────────────────────────────

    @Test
    @DisplayName("cashIn – polymorphic execute() via BankingOperation interface")
    void testCashInViaPolymorphicInterface() {
        // CashIn assigned to BankingOperation — polymorphism
        BankingOperation op = cashIn
                .forUser(1)
                .amount(200.00)
                .description("7-Eleven")
                .from(0);

        assertTrue(executeOperation(op),   // inherited helper from BaseTest
                   "CashIn.execute() via BankingOperation should return true");
    }

    // ─── BALANCE UPDATES ──────────────────────────────────────────────────

    @Test
    @DisplayName("cashIn PHP 200 – balance goes from PHP 5000 to PHP 5200")
    void testCashIn200UpdatesBalance() {
        double before = checkBalance.checkBalance(1);    // 5000.00
        cashIn.cashIn(1, 200.00, "Cash-in via 7-Eleven", 0);
        double after  = checkBalance.checkBalance(1);

        assertAll("cash-in PHP 200",
            () -> assertWith(after, equalsAmount(5200.00),       "Exact: PHP 5200.00"),
            () -> assertWith(after, equalsAmount(before + 200),  "Delta: +200")
        );
    }

    @Test
    @DisplayName("cashIn PHP 300 – balance goes from PHP 5000 to PHP 5300")
    void testCashIn300UpdatesBalance() {
        double before = checkBalance.checkBalance(1);
        cashIn.cashIn(1, 300.00, "Cash-in via Palawan Express", 0);

        assertWith(checkBalance.checkBalance(1), equalsAmount(before + 300.00),
                   "Balance should be PHP 5300.00 after +300");
    }

    @Test
    @DisplayName("cashIn – sequential PHP 200 + PHP 300 → total PHP 5500")
    void testSequentialCashInsAccumulate() {
        cashIn.cashIn(1, 200.00, "First",  0);
        cashIn.cashIn(1, 300.00, "Second", 0);

        // Predicate for the exact accumulated value
        Predicate<Double> is5500 = equalsAmount(5500.00);
        assertWith(checkBalance.checkBalance(1), is5500,
                   "5000 + 200 + 300 must equal PHP 5500.00");
    }

    @Test
    @DisplayName("cashIn – returns true on every valid cash-in")
    void testCashInReturnsTrueOnSuccess() {
        assertAll("valid cash-ins return true",
            () -> assertOperation(() -> cashIn.cashIn(1, 1.00,    "Min amount",  0), true, "PHP 1.00"),
            () -> assertOperation(() -> cashIn.cashIn(2, 500.00,  "Normal",      0), true, "PHP 500"),
            () -> assertOperation(() -> cashIn.cashIn(3, 9999.00, "Large",       0), true, "PHP 9999")
        );
    }

    // ─── VALID_AMOUNT PREDICATE ───────────────────────────────────────────

    @Test
    @DisplayName("CashIn.VALID_AMOUNT – validates boundary amounts correctly")
    void testValidAmountPredicate() {
        Predicate<Double> valid = CashIn.VALID_AMOUNT;

        assertAll("VALID_AMOUNT predicate",
            () -> assertTrue (valid.test(1.00),    "PHP 1.00 is minimum valid"),
            () -> assertTrue (valid.test(500.00),  "PHP 500 is valid"),
            () -> assertFalse(valid.test(0.99),    "PHP 0.99 below minimum"),
            () -> assertFalse(valid.test(0.00),    "PHP 0.00 invalid"),
            () -> assertFalse(valid.test(-100.0),  "Negative invalid"),
            () -> assertFalse(valid.test(null),    "Null invalid")
        );
    }

    // ─── TRANSACTION LOGGING ──────────────────────────────────────────────

    @Test
    @DisplayName("cashIn – transaction is logged with correct type (CASH-IN)")
    void testCashInLogsCorrectTransactionType() {
        cashIn.cashIn(1, 200.00, "Log test", 0);

        long cashInCount = countWhere(
                transactions.viewAll(),
                Transactions.byUser(1).and(Transactions.isCashIn())
        );
        assertEquals(1L, cashInCount,
                "Exactly 1 CASH-IN transaction should be logged for User 1");
    }

    // ─── REJECTION SCENARIOS ──────────────────────────────────────────────

    @Test
    @DisplayName("cashIn – rejected inputs do not change the balance")
    void testRejectedCashInsLeaveBalanceUnchanged() {
        double before = checkBalance.checkBalance(1);   // PHP 5000.00

        assertAll("all invalid cash-ins are rejected",
            () -> assertOperation(() -> cashIn.cashIn(1, -100.00, "neg",   0), false, "negative"),
            () -> assertOperation(() -> cashIn.cashIn(1,  0.00,   "zero",  0), false, "zero"),
            () -> assertOperation(() -> cashIn.cashIn(1,  0.99,   "low",   0), false, "below min"),
            () -> assertOperation(() -> cashIn.cashIn(99, 100.00, "ghost", 0), false, "unknown user"),
            () -> assertOperation(() -> cashIn.cashIn(0,  100.00, "id0",   0), false, "id=0")
        );

        assertWith(checkBalance.checkBalance(1), equalsAmount(before),
                "Balance must remain PHP 5000.00 after all rejected cash-ins");
    }
}
